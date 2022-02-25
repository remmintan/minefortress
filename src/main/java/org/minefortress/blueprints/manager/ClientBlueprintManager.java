package org.minefortress.blueprints.manager;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.minefortress.blueprints.data.BlueprintBlockData;
import org.minefortress.blueprints.data.BlueprintDataLayer;
import org.minefortress.blueprints.data.ClientBlueprintBlockDataManager;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.ServerboundBlueprintTaskPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.renderer.gui.blueprints.BlueprintGroup;
import org.minefortress.tasks.BuildingManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClientBlueprintManager {

    private final MinecraftClient client;
    private final ClientBlueprintBlockDataManager blockDataManager = new ClientBlueprintBlockDataManager();
    private final BlueprintMetadataManager blueprintMetadataManager = new BlueprintMetadataManager();

    private BlueprintMetadata selectedStructure;
    private BlockPos blueprintBuildPos = null;
    private boolean cantBuild = false;

    public ClientBlueprintManager(MinecraftClient client) {
        this.client = client;
    }

    public BlockPos getBlueprintBuildPos() {
        return blueprintBuildPos;
    }

    public void tick() {
        if(!hasSelectedBlueprint()) return;
        blueprintBuildPos = getSelectedPos();
        if(blueprintBuildPos == null) return;
        checkCantBuild();
    }

    private void checkCantBuild() {
        final BlueprintBlockData blockData = blockDataManager
                .getBlockData(selectedStructure.getFile(), selectedStructure.getRotation());
        final Set<BlockPos> blueprintDataPositions = blockData.getLayer(BlueprintDataLayer.GENERAL).keySet();
        final boolean blueprintPartInTheSurface = blueprintDataPositions.stream()
                .filter(blockPos -> !(blockData.isStandsOnGrass() && blockPos.getY() == 0))
                .map(pos -> pos.add(blueprintBuildPos))
                .anyMatch(pos -> !BuildingManager.canPlaceBlock(client.world, pos));

        final boolean blueprintPartInTheAir = blueprintDataPositions.stream()
                .filter(blockPos -> {
                    if (blockData.isStandsOnGrass()) {
                        return blockPos.getY() == 1 && !blueprintDataPositions.contains(blockPos.down());
                    } else {
                        return blockPos.getY() == 0;
                    }
                })
                .map(pos -> pos.add(blueprintBuildPos))
                .anyMatch(pos -> BuildingManager.canPlaceBlock(client.world, pos.down()));

        cantBuild = blueprintPartInTheSurface || blueprintPartInTheAir;
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

        final boolean posSolid = !BuildingManager.doesNotHaveCollisions(client.world, pos);
        final BlueprintBlockData blockData = blockDataManager
                .getBlockData(selectedStructure.getFile(), selectedStructure.getRotation());
        final Vec3i size = blockData.getSize();
        final Vec3i halfSize = new Vec3i(size.getX() / 2, 0, size.getZ() / 2);
        BlockPos movedPos = pos.subtract(halfSize);
        movedPos = blockData.isStandsOnGrass() ? movedPos.down() : movedPos;
        movedPos = posSolid? movedPos.up():movedPos;
        return movedPos;
    }

    public boolean hasSelectedBlueprint() {
        return selectedStructure != null;
    }

    public void select(BlueprintMetadata blueprintMetadata) {
        this.selectedStructure = blueprintMetadata;
        this.blueprintMetadataManager.select(blueprintMetadata);
    }

    public void selectNext() {
        if(!this.hasSelectedBlueprint()) return;
        this.selectedStructure = blueprintMetadataManager.selectNext();
    }

    public List<BlueprintMetadata> getAllBlueprints(BlueprintGroup group) {
        return this.blueprintMetadataManager.getAllForGroup(group);
    }

    public void buildCurrentStructure() {
        if(selectedStructure == null) throw new IllegalStateException("No blueprint selected");
        if(blueprintBuildPos == null) throw new IllegalStateException("No blueprint build position");

        if(cantBuild) return;

        UUID taskId = UUID.randomUUID();
        final FortressClientWorld world = (FortressClientWorld) client.world;
        if(world != null) {
            final Map<BlockPos, BlockState> structureData = blockDataManager
                    .getBlockData(selectedStructure.getFile(), selectedStructure.getRotation())
                    .getLayer(BlueprintDataLayer.GENERAL);
            final List<BlockPos> blocks = structureData
                    .keySet()
                    .stream()
                    .map(it -> it.add(blueprintBuildPos))
                    .collect(Collectors.toList());
            world.getClientTasksHolder().addTask(taskId, blocks);
        }
        final ServerboundBlueprintTaskPacket serverboundBlueprintTaskPacket = new ServerboundBlueprintTaskPacket(taskId, selectedStructure.getId(), selectedStructure.getFile(), blueprintBuildPos, selectedStructure.getRotation());
        FortressClientNetworkHelper.send(FortressChannelNames.NEW_BLUEPRINT_TASK, serverboundBlueprintTaskPacket);

        if(!client.options.keySprint.isPressed()) {
            clearStructure();
        }
    }

    public void clearStructure() {
        this.selectedStructure = null;
    }

    public BlueprintMetadata getSelectedStructure() {
        return selectedStructure;
    }

    public void rotateSelectedStructureClockwise() {
        if(selectedStructure == null) throw new IllegalStateException("No blueprint selected");
        this.selectedStructure.rotateRight();
    }

    public void rotateSelectedStructureCounterClockwise() {
        if(selectedStructure == null) throw new IllegalStateException("No blueprint selected");
        this.selectedStructure.rotateLeft();
    }

    public boolean isCantBuild() {
        return cantBuild;
    }

    public ClientBlueprintBlockDataManager getBlockDataManager() {
        return blockDataManager;
    }

    public void add(BlueprintGroup group, String name, String file, int floorLevel, NbtCompound tag) {
        final BlueprintMetadata metadata = this.blueprintMetadataManager.add(group, name, file, floorLevel);
        blockDataManager.setBlueprint(metadata.getFile(), tag);
        blockDataManager.invalidateBlueprint(metadata.getFile());
    }

    public void update(String fileName, NbtCompound tag) {
        blockDataManager.setBlueprint(fileName, tag);
        blockDataManager.invalidateBlueprint(fileName);
        if(client instanceof FortressMinecraftClient fortressClient) {
            fortressClient.getBlueprintRenderer().getBlueprintsModelBuilder().invalidateBlueprint(fileName);
        }
    }

    public void reset() {
        this.clearStructure();
        this.blueprintMetadataManager.reset();
        this.blockDataManager.reset();
        if(client instanceof FortressMinecraftClient fortressClient) {
            fortressClient.getBlueprintRenderer().getBlueprintsModelBuilder().reset();
        }
    }

}
