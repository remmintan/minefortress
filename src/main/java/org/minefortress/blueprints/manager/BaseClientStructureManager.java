package org.minefortress.blueprints.manager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.remmintan.mods.minefortress.building.BuildingHelper;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.*;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import org.jetbrains.annotations.Nullable;
import org.minefortress.utils.ModUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseClientStructureManager implements IStructureRenderInfoProvider {

    private final MinecraftClient client;
    private boolean enoughResources = true;
    private boolean cantBuild = false;

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
        checkCantBuild();
    }
    protected BlockPos getStructureBuildPos() {
        return structureBuildPos;
    }
    private void checkNotEnoughResources() {
        final var fortressClientManager = ((IClientManagersProvider)client).get_ClientFortressManager();
        if(fortressClientManager.isSurvival()) {
            final var resourceManager = fortressClientManager.getResourceManager();
            final var stacks = getBlockData().getStacks();
            enoughResources = resourceManager.hasItems(stacks);
        } else {
            enoughResources = true;
        }
    }

    protected boolean isEnoughResources() {
        return enoughResources;
    }

    private void checkCantBuild() {
        if(!enoughResources) {
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

        final var fortressBorder = ModUtils.getInfluenceManager().getFortressBorder();

        final boolean blueprintPartInTheSurface = blueprintDataPositions.stream()
                .filter(blockPos -> blockPos.getY() >= floorLevel)
                .map(pos -> pos.add(structureBuildPos.down(floorLevel)))
                .anyMatch(pos -> !BuildingHelper.canPlaceBlock(client.world, pos) ||
                        fortressBorder.map(border -> !border.contains(pos)).orElse(false));

        final boolean blueprintPartInTheAir = blueprintDataPositions.stream()
                .filter(blockPos -> {
                    final int y = blockPos.getY();
                    return y<=floorLevel;
                })
                .map(pos -> pos.add(structureBuildPos.down(floorLevel)))
                .anyMatch(pos -> BuildingHelper.canPlaceBlock(client.world, pos.down()) ||
                        fortressBorder.map(border -> !border.contains(pos)).orElse(false));

        cantBuild = blueprintPartInTheSurface || blueprintPartInTheAir;
    }

    private IStructureBlockData getBlockData() {
        final var blockDataProvider = getBlockDataProvider();
        return blockDataProvider.getBlockData(getSelectedStructure().getId(), getSelectedRotation().getRotation());
    }

    @Nullable
    private BlockPos getSelectedPos() {
        if(client.crosshairTarget instanceof final BlockHitResult crosshairTarget) {
            final BlockPos originalPos = crosshairTarget.getBlockPos();
            if (client.world != null && originalPos != null && client.world.getBlockState(originalPos).isAir()) return null;
            if(originalPos != null) return moveToStructureSize(originalPos);
        }
        return null;
    }

    private BlockPos moveToStructureSize(BlockPos pos) {
        if(getSelectedStructure() == null) return pos;

        final boolean posSolid = !BuildingHelper.doesNotHaveCollisions(client.world, pos);
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

    protected void reset() {
        structureBuildPos = null;
        cantBuild = false;
        enoughResources = true;
        getBlockDataProvider().reset();
    }

}
