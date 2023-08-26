package org.minefortress.blueprints.manager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import org.minefortress.blueprints.data.ClientStructureBlockDataProvider;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.c2s.ServerboundBlueprintTaskPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.renderer.gui.blueprints.BlueprintGroup;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class ClientBlueprintManager extends BaseClientStructureManager {

    private final MinecraftClient client;
    private final ClientStructureBlockDataProvider blockDataManager = new ClientStructureBlockDataProvider();
    private final BlueprintMetadataManager blueprintMetadataManager = new BlueprintMetadataManager();

    private BlueprintMetadata selectedStructure;


    public ClientBlueprintManager(MinecraftClient client) {
        super(client);
        this.client = client;
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
        if(super.getStructureRenderPos().isEmpty()) {
            LoggerFactory.getLogger(ClientBlueprintManager.class).error("No position selected");
            return;
        }

        if(!super.canBuild()) return;

        UUID taskId = UUID.randomUUID();
        addTaskToTasksHolder(taskId);
        final ServerboundBlueprintTaskPacket serverboundBlueprintTaskPacket =
                new ServerboundBlueprintTaskPacket(taskId,
                        selectedStructure.getId(),
                        getStructureBuildPos(),
                        selectedStructure.getRotation(),
                        getSelectedStructure().getFloorLevel());
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

    @Override
    public ClientStructureBlockDataProvider getBlockDataProvider() {
        return blockDataManager;
    }

    public void add(BlueprintGroup group, String name, String file, int floorLevel, String requirementId, NbtCompound tag) {
        final BlueprintMetadata metadata = this.blueprintMetadataManager.add(group, name, file, floorLevel, requirementId);
        blockDataManager.setBlueprint(metadata.getId(), tag);
        blockDataManager.invalidateBlueprint(metadata.getId());
    }

    public void update(String fileName, NbtCompound tag, int newFloorLevel) {
        blueprintMetadataManager.update(fileName,newFloorLevel);

        blockDataManager.setBlueprint(fileName, tag);
        blockDataManager.invalidateBlueprint(fileName);
        if(client instanceof FortressMinecraftClient fortressClient) {
            fortressClient.get_BlueprintRenderer().getBlueprintsModelBuilder().invalidateBlueprint(fileName);
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
                fortressClient.get_BlueprintRenderer().getBlueprintsModelBuilder().reset();
            }catch (Exception ignore) {}
        }
    }

}
