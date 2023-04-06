package org.minefortress.blueprints.manager;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.minefortress.blueprints.data.StrctureBlockData;
import org.minefortress.blueprints.data.BlueprintDataLayer;
import org.minefortress.blueprints.data.ClientStructureBlockDataProvider;
import org.minefortress.blueprints.interfaces.IStructureRenderInfoProvider;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.c2s.ServerboundBlueprintTaskPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.renderer.gui.blueprints.BlueprintGroup;
import org.minefortress.utils.BuildingHelper;
import org.minefortress.utils.ModUtils;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ClientBlueprintManager implements IStructureRenderInfoProvider {

    private final MinecraftClient client;
    private final ClientStructureBlockDataProvider blockDataManager = new ClientStructureBlockDataProvider();
    private final BlueprintMetadataManager blueprintMetadataManager = new BlueprintMetadataManager();

    private BlueprintMetadata selectedStructure;
    private BlockPos blueprintBuildPos = null;
    private boolean enoughResources = true;
    private boolean cantBuild = false;

    public ClientBlueprintManager(MinecraftClient client) {
        this.client = client;
    }

    public Optional<BlockPos> getStructureRenderPos() {
        final var floorLevel = Optional.ofNullable(getSelectedStructure()).map(BlueprintMetadata::getFloorLevel).orElse(0);
        return Optional.ofNullable(blueprintBuildPos).map(it -> it.down(floorLevel));
    }

    public void tick() {
        if(!isSelecting()) return;
        blueprintBuildPos = getSelectedPos();
        if(blueprintBuildPos == null) return;
        checkNotEnoughResources();
        checkCantBuild();
    }

    private void checkNotEnoughResources() {
        final var fortressClient = (FortressMinecraftClient) this.client;
        final var fortressClientManager = fortressClient.getFortressClientManager();
        if(fortressClientManager.isSurvival()) {
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
        final StrctureBlockData blockData = getBlockData();
        final Set<BlockPos> blueprintDataPositions = blockData.getLayer(BlueprintDataLayer.GENERAL)
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isAir())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        final int floorLevel = selectedStructure.getFloorLevel();

        final var fortressBorder = ModUtils.getFortressClientManager().getFortressBorder();

        final boolean blueprintPartInTheSurface = blueprintDataPositions.stream()
                .filter(blockPos -> blockPos.getY() >= floorLevel)
                .map(pos -> pos.add(blueprintBuildPos.down(floorLevel)))
                .anyMatch(pos -> !BuildingHelper.canPlaceBlock(client.world, pos) ||
                        fortressBorder.map(border -> !border.contains(pos)).orElse(false));

        final boolean blueprintPartInTheAir = blueprintDataPositions.stream()
                .filter(blockPos -> {
                    final int y = blockPos.getY();
                    return y<=floorLevel;
                })
                .map(pos -> pos.add(blueprintBuildPos.down(floorLevel)))
                .anyMatch(pos -> BuildingHelper.canPlaceBlock(client.world, pos.down()) ||
                        fortressBorder.map(border -> !border.contains(pos)).orElse(false));

        cantBuild = blueprintPartInTheSurface || blueprintPartInTheAir;
    }

    private StrctureBlockData getBlockData() {
        return blockDataManager
                .getBlockData(selectedStructure.getFile(), selectedStructure.getRotation());
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
        if(selectedStructure == null) return pos;

        final boolean posSolid = !BuildingHelper.doesNotHaveCollisions(client.world, pos);
        final StrctureBlockData blockData = getBlockData();
        final Vec3i size = blockData.getSize();
        final Vec3i halfSize = new Vec3i(size.getX() / 2, 0, size.getZ() / 2);
        BlockPos movedPos = pos.subtract(halfSize);
        movedPos = posSolid? movedPos.up():movedPos;
        return movedPos;
    }

    public boolean isSelecting() {
        return selectedStructure != null;
    }

    public void select(BlueprintMetadata blueprintMetadata) {
        this.selectedStructure = blueprintMetadata;
        this.blueprintMetadataManager.select(blueprintMetadata);
    }

    public void selectNext() {
        if(!this.isSelecting()) return;
        this.selectedStructure = blueprintMetadataManager.selectNext();
    }

    public List<BlueprintMetadata> getAllBlueprints(BlueprintGroup group) {
        return this.blueprintMetadataManager.getAllForGroup(group);
    }

    public void buildCurrentStructure() {
        if(selectedStructure == null) {
            LoggerFactory.getLogger(ClientBlueprintManager.class).error("No structure selected");
            return;
        }
        if(blueprintBuildPos == null) {
            LoggerFactory.getLogger(ClientBlueprintManager.class).error("No position selected");
            return;
        }

        if(cantBuild) return;

        UUID taskId = UUID.randomUUID();
        final FortressClientWorld world = (FortressClientWorld) client.world;
        if(world != null) {
            final StrctureBlockData blockData = getBlockData();
            final Map<BlockPos, BlockState> structureData = blockData
                    .getLayer(BlueprintDataLayer.GENERAL);
            final int floorLevel = selectedStructure.getFloorLevel();
            final List<BlockPos> blocks = structureData
                    .entrySet()
                    .stream()
                    .filter(entry -> !entry.getValue().isAir())
                    .map(Map.Entry::getKey)
                    .map(it -> it.add(blueprintBuildPos.down(floorLevel)))
                    .collect(Collectors.toList());
            world.getClientTasksHolder().addTask(taskId, blocks);
        }
        final ServerboundBlueprintTaskPacket serverboundBlueprintTaskPacket = new ServerboundBlueprintTaskPacket(taskId, selectedStructure.getId(), selectedStructure.getFile(), blueprintBuildPos, selectedStructure.getRotation(), getSelectedStructure().getFloorLevel());
        FortressClientNetworkHelper.send(FortressChannelNames.NEW_BLUEPRINT_TASK, serverboundBlueprintTaskPacket);

        if(!client.options.sprintKey.isPressed()) {
            clearStructure();
        }
    }

    public void clearStructure() {
        this.selectedStructure = null;
    }

    public BlueprintMetadata getSelectedStructure() {
        return selectedStructure;
    }

    public BlueprintMetadataManager getBlueprintMetadataManager() {
        return blueprintMetadataManager;
    }

    public void rotateSelectedStructureClockwise() {
        if(selectedStructure == null) throw new IllegalStateException("No blueprint selected");
        this.selectedStructure.rotateRight();
    }

    public void rotateSelectedStructureCounterClockwise() {
        if(selectedStructure == null) throw new IllegalStateException("No blueprint selected");
        this.selectedStructure.rotateLeft();
    }

    public boolean canBuild() {
        return !cantBuild;
    }

    public ClientStructureBlockDataProvider getBlockDataManager() {
        return blockDataManager;
    }

    public void add(BlueprintGroup group, String name, String file, int floorLevel, String requirementId, NbtCompound tag) {
        final BlueprintMetadata metadata = this.blueprintMetadataManager.add(group, name, file, floorLevel, requirementId);
        blockDataManager.setBlueprint(metadata.getFile(), tag);
        blockDataManager.invalidateBlueprint(metadata.getFile());
    }

    public void update(String fileName, NbtCompound tag, int newFloorLevel) {
        blueprintMetadataManager.update(fileName,newFloorLevel);

        blockDataManager.setBlueprint(fileName, tag);
        blockDataManager.invalidateBlueprint(fileName);
        if(client instanceof FortressMinecraftClient fortressClient) {
            fortressClient.getBlueprintRenderer().getBlueprintsModelBuilder().invalidateBlueprint(fileName);
        }
    }

    public void remove(String filename) {
        blueprintMetadataManager.remove(filename);
        blockDataManager.removeBlueprint(filename);
        blockDataManager.invalidateBlueprint(filename);
    }

    public void reset() {
        this.clearStructure();
        this.blueprintMetadataManager.reset();
        this.blockDataManager.reset();
        if(client instanceof FortressMinecraftClient fortressClient) {
            try {
                fortressClient.getBlueprintRenderer().getBlueprintsModelBuilder().reset();
            }catch (Exception ignore) {}
        }
    }

}
