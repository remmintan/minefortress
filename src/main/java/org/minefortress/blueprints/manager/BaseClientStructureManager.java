package org.minefortress.blueprints.manager;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockBox;
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
        if (structureBuildPos == null) {
            cantBuild = true;
            enoughResources = true; // Reset to avoid false positives if placement becomes valid later
            intersectsUpgradingBuilding = true; // Reset for the same reason
            return;
        }
        // Reset cantBuild before checks if structureBuildPos is valid
        cantBuild = false;
        checkNotEnoughResources(); // This might set cantBuild = true
        if (cantBuild) return; // Early exit if not enough resources

        // For upgrading logic
        if (this instanceof ClientBlueprintManager clientBlueprintManager && clientBlueprintManager.isUpgrading()) {
            checkIntersectUpgradingBuilding(); // This might set cantBuild = true (via intersectsUpgradingBuilding)
            if (!intersectsUpgradingBuilding) {
                cantBuild = true;
            }
            if (cantBuild) return; // Early exit if upgrade intersection fails
        } else {
            intersectsUpgradingBuilding = true; // Default to true if not upgrading
        }


        checkPlacementValidity(); // Combined check for leaves, surface collision, and sub-floor validity
    }
    protected BlockPos getStructureBuildPos() {
        return structureBuildPos;
    }
    private void checkNotEnoughResources() {
        final var selectedStructure = getSelectedStructure();
        if (selectedStructure == null) { // Should not happen if isSelecting() is true, but good for safety
            enoughResources = false;
            cantBuild = true;
            return;
        }
        final var campfire = selectedStructure.getId().equals("campfire");
        if (ClientExtensionsKt.isSurvivalFortress(MinecraftClient.getInstance()) && !campfire) {
            final var fortressClientManager = ClientModUtils.getFortressManager();
            final var resourceManager = fortressClientManager.getResourceManager();
            final var stacks = getBlockData().getStacks();
            enoughResources = resourceManager.hasItems(stacks);
        } else {
            enoughResources = true;
        }
        if(!enoughResources) {
            cantBuild = true;
        }
    }

    private void checkPlacementValidity() {
        if (cantBuild) return; // If already marked as can't build (e.g. due to resources), skip further checks

        final IStructureBlockData blockData = getBlockData();
        final Set<BlockPos> blueprintDataPositions = blockData.getLayer(BlueprintDataLayer.GENERAL)
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isAir()) // Only consider non-air blocks from the blueprint
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        final int floorLevel = getSelectedStructure().getFloorLevel();
        final Vec3i size = blockData.getSize(); // For leaves check

        // 1. Check for leaves directly under the blueprint's entire footprint
        final int groundYInWorld = structureBuildPos.getY() - floorLevel - 1;
        for (int dx = 0; dx < size.getX(); dx++) {
            for (int dz = 0; dz < size.getZ(); dz++) {
                BlockPos currentGroundPos = new BlockPos(
                        structureBuildPos.getX() + dx,
                        groundYInWorld,
                        structureBuildPos.getZ() + dz
                );
                if (this.client.world != null) {
                    BlockState stateBeneath = this.client.world.getBlockState(currentGroundPos);
                    if (stateBeneath.isIn(BlockTags.LEAVES)) {
                        cantBuild = true;
                        return;
                    }
                }
            }
        }

        // 2. Check blocks AT OR ABOVE the blueprint's floor level
        final boolean isUpgrading = (this instanceof ClientBlueprintManager cbm) && cbm.isUpgrading();
        final BlockBox upgradingBox = isUpgrading ? ((ClientBlueprintManager) this).getUpgradingBuildingBox() : null;

        final boolean partAtOrAboveFloorCollides = blueprintDataPositions.stream()
                .filter(bpLocalPos -> bpLocalPos.getY() >= floorLevel)
                .map(bpLocalPos -> structureBuildPos.add(bpLocalPos.getX(), bpLocalPos.getY() - floorLevel, bpLocalPos.getZ()))
                .anyMatch(worldPos -> {
                    boolean currentCanBuild = BuildingHelper.canPlaceBlock(client.world, worldPos);
                    if (isUpgrading && upgradingBox != null) {
                        currentCanBuild = currentCanBuild || upgradingBox.contains(worldPos);
                    }
                    return !currentCanBuild; // if !canPlace (and not part of upgrade), then it collides
                });

        if (partAtOrAboveFloorCollides) {
            cantBuild = true;
            return;
        }

        // 3. Check blocks BELOW the blueprint's floor level
        final boolean partBelowFloorInvalid = blueprintDataPositions.stream()
                .filter(bpLocalPos -> bpLocalPos.getY() < floorLevel)
                .map(bpLocalPos -> structureBuildPos.add(bpLocalPos.getX(), bpLocalPos.getY() - floorLevel, bpLocalPos.getZ()))
                .anyMatch(worldPos -> BuildingHelper.canPlaceBlock(client.world, worldPos));

        if (partBelowFloorInvalid) {
            cantBuild = true;
            // No return here, as we've already checked other conditions that might set cantBuild
        }
    }


    private void checkIntersectUpgradingBuilding() { // This method is only relevant if isUpgrading is true
        if (!(this instanceof ClientBlueprintManager cbm) || !cbm.isUpgrading()) {
            intersectsUpgradingBuilding = true; // Not upgrading, so no intersection issue by default
            return;
        }

        if (!enoughResources) { // If not enough resources, intersection check is moot for buildability
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
        final BlockBox upgradingBuildingBox = cbm.getUpgradingBuildingBox();

        // An upgrade is valid if at least one block of the new blueprint (at or above its floor)
        // intersects with the existing building's box.
        intersectsUpgradingBuilding = blueprintDataPositions.stream()
                .filter(bpLocalPos -> bpLocalPos.getY() >= floorLevel) // Consider blocks at or above the blueprint's designated floor for intersection
                .map(bpLocalPos -> structureBuildPos.add(bpLocalPos.getX(), bpLocalPos.getY() - floorLevel, bpLocalPos.getZ()))
                .anyMatch(upgradingBuildingBox::contains);
    }

    private IStructureBlockData getBlockData() {
        final var blockDataProvider = getBlockDataProvider();
        final var selected = getSelectedStructure();
        if (selected == null) throw new IllegalStateException("No structure selected");
        final var rotation = getSelectedRotation();
        if (rotation == null) throw new IllegalStateException("No rotation selected");
        return blockDataProvider.getBlockData(selected.getId(), rotation.getRotation());
    }

    @Nullable
    private BlockPos getSelectedPos() {
        if (client.crosshairTarget instanceof final BlockHitResult crosshairTarget && this instanceof ClientBlueprintManager cbm) {
            final BlockPos originalPos = crosshairTarget.getBlockPos();
            if (client.world == null || originalPos == null) return null; // Ensure world is not null

            final BlockState blockState = client.world.getBlockState(originalPos);
            if (blockState.isAir() && !cbm.isUpgrading()) return null; // Can't start placement on air unless upgrading

            var movedPos = originalPos;
            final BlockBox upgradingBuildingBox = cbm.getUpgradingBuildingBox(); // Can be null if not upgrading
            final boolean intersectingWithTheBuilding = cbm.isUpgrading() && upgradingBuildingBox != null &&
                    (upgradingBuildingBox.contains(originalPos) || blockState.isOf(FortressBlocks.FORTRESS_BUILDING));

            if (intersectingWithTheBuilding) {
                // Align Y with the bottom of the existing building box if upgrading
                movedPos = new BlockPos(movedPos.getX(), upgradingBuildingBox.getMinY(), movedPos.getZ());
            }

            return moveToStructureSize(movedPos, intersectingWithTheBuilding);

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
        // If placing on a solid block (and not upgrading an existing structure at that spot),
        // move the placement position up by one.
        if (posSolid) {
            movedPos = movedPos.up();
        }
        return movedPos;
    }

    @Override
    public final Optional<BlockPos> getStructureRenderPos() {
        final var selected = getSelectedStructure();
        if (selected == null) return Optional.empty();
        final var floorLevel = selected.getFloorLevel();
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
        intersectsUpgradingBuilding = true; // Default to true, will be set to false if upgrading and not intersecting
        getBlockDataProvider().reset();
    }

}