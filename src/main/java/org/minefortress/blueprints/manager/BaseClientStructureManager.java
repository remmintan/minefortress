package org.minefortress.blueprints.manager;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.remmintan.mods.minefortress.blocks.FortressBlocks;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.*;
import net.remmintan.mods.minefortress.core.utils.BuildingHelper;
import net.remmintan.mods.minefortress.core.utils.ClientExtensionsKt;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseClientStructureManager implements IStructureRenderInfoProvider {

    private final MinecraftClient client;
    private boolean enoughResources = true;
    private boolean cantBuild = false;
    private boolean intersectsUpgradingBuilding = false;

    private BlockPos structureBuildPos = null;

    protected BaseClientStructureManager(MinecraftClient client) {
        this.client = client;
    }

    protected abstract IBlockDataProvider getBlockDataProvider();

    public abstract BlueprintMetadata getSelectedStructure();

    public abstract IBlueprintRotation getSelectedRotation();
    public void tick() {
        if(!isSelecting()) return;
        structureBuildPos = getSelectedPos();
        if(structureBuildPos == null) return;
        checkNotEnoughResources();
        checkIntersectUpgradingBuilding();
        checkCantBuild();
    }
    protected BlockPos getStructureBuildPos() {
        return structureBuildPos;
    }
    private void checkNotEnoughResources() {
        final var campfire = getSelectedStructure().getId().equals("campfire");
        if (ClientExtensionsKt.isSurvivalFortress(MinecraftClient.getInstance()) && !campfire) {
            final var fortressClientManager = ClientModUtils.getFortressManager();
            final var resourceManager = fortressClientManager.getResourceManager();
            final var stacks = getBlockData().getStacks();
            enoughResources = resourceManager.hasItems(stacks);
        } else {
            enoughResources = true;
        }
    }

    private void checkCantBuild() {
        if(!enoughResources) {
            cantBuild = true;
            return;
        }

        if (!intersectsUpgradingBuilding) {
            cantBuild = true;
            return;
        }

        final IStructureBlockData blockData = getBlockData();
        final Set<BlockPos> blueprintDataPositions = blockData.getLayer(BlueprintDataLayer.GENERAL)
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isAir())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        final int floorLevel = getSelectedStructure().getFloorLevel();


        if (this instanceof ClientBlueprintManager clientBlueprintManager) {
            final var upgrading = clientBlueprintManager.isUpgrading();
            final var upgradingBuildingBox = clientBlueprintManager.getUpgradingBuildingBox();

            final boolean blueprintPartInTheSurface = blueprintDataPositions.stream()
                    .filter(blockPos -> blockPos.getY() >= floorLevel)
                    .map(pos -> pos.add(structureBuildPos.down(floorLevel)))
                    .anyMatch(pos -> {
                        boolean canBuild = BuildingHelper.canPlaceBlock(client.world, pos);

                        if (upgrading) {
                            canBuild = canBuild || upgradingBuildingBox.contains(pos);
                        }

                        return !canBuild;
                    });

            final boolean blueprintPartInTheAir = blueprintDataPositions.stream()
                    .filter(blockPos -> {
                        final int y = blockPos.getY();
                        return y <= floorLevel;
                    })
                    .map(pos -> pos.add(structureBuildPos.down(floorLevel)))
                    .anyMatch(pos -> BuildingHelper.canPlaceBlock(client.world, pos.down()));


            cantBuild = blueprintPartInTheSurface || blueprintPartInTheAir;
        } else {
            throw new IllegalStateException("This class should be ClientBlueprintManager");
        }


    }

    private void checkIntersectUpgradingBuilding() {

        if (this instanceof ClientBlueprintManager cbm) {
            final var upgrading = cbm.isUpgrading();
            if (!upgrading) {
                intersectsUpgradingBuilding = true;
                return;
            }

            if (!enoughResources) {
                intersectsUpgradingBuilding = false;
                return;
            }

            final IStructureBlockData blockData = getBlockData();
            final Set<BlockPos> blueprintDataPositions = blockData.getLayer(BlueprintDataLayer.GENERAL)
                    .entrySet()
                    .stream()
                    .filter(entry -> !entry.getValue().isAir())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            final int floorLevel = getSelectedStructure().getFloorLevel();

            final var upgradingBuildingBox = cbm.getUpgradingBuildingBox();

            intersectsUpgradingBuilding = blueprintDataPositions.stream()
                    .filter(blockPos -> blockPos.getY() >= floorLevel)
                    .map(pos -> pos.add(structureBuildPos.down(floorLevel)))
                    .anyMatch(upgradingBuildingBox::contains);
        } else {
            throw new IllegalStateException("This class should be ClientBlueprintManager");
        }
    }

    private IStructureBlockData getBlockData() {
        final var blockDataProvider = getBlockDataProvider();
        return blockDataProvider.getBlockData(getSelectedStructure().getId(), getSelectedRotation().getRotation());
    }

    @Nullable
    private BlockPos getSelectedPos() {
        if (client.crosshairTarget instanceof final BlockHitResult crosshairTarget && this instanceof ClientBlueprintManager cbm) {
            final BlockPos originalPos = crosshairTarget.getBlockPos();
            if (originalPos != null) {
                final var blockState = client.world != null ? client.world.getBlockState(originalPos) : Blocks.AIR.getDefaultState();
                if (blockState.isAir()) return null;
                var movedPos = originalPos;
                final var upgradingBuildingBox = cbm.getUpgradingBuildingBox();
                final var intersectingWithTheBuilding = cbm.isUpgrading() && (upgradingBuildingBox.contains(originalPos) || blockState.isOf(FortressBlocks.FORTRESS_BUILDING));
                if (intersectingWithTheBuilding) {
                    movedPos = new BlockPos(movedPos.getX(), upgradingBuildingBox.getMinY(), movedPos.getZ());
                }

                return moveToStructureSize(movedPos, intersectingWithTheBuilding);
            }
        }
        return null;
    }

    private BlockPos moveToStructureSize(BlockPos pos, boolean intersectingWithTheBuilding) {
        if(getSelectedStructure() == null) return pos;

        final boolean posSolid = BuildingHelper.hasCollisions(client.world, pos) && !intersectingWithTheBuilding;
        final IStructureBlockData blockData = getBlockData();
        final Vec3i size = blockData.getSize();
        final Vec3i halfSize = new Vec3i(size.getX() / 2, 0, size.getZ() / 2);
        BlockPos movedPos = pos.subtract(halfSize);
        movedPos = posSolid? movedPos.up():movedPos;
        return movedPos;
    }

    @Override
    public final Optional<BlockPos> getStructureRenderPos() {
        final var floorLevel = Optional.ofNullable(getSelectedStructure()).map(BlueprintMetadata::getFloorLevel).orElse(0);
        return Optional.ofNullable(structureBuildPos).map(it -> it.down(floorLevel));
    }

    @Override
    public boolean canBuild() {
        return !cantBuild;
    }

    @Override
    public boolean intersectsUpgradingBuilding() {
        return intersectsUpgradingBuilding;
    }

    protected void reset() {
        structureBuildPos = null;
        cantBuild = false;
        enoughResources = true;
        getBlockDataProvider().reset();
    }

}
