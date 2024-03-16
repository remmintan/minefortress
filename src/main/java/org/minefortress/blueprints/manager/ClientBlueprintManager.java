package org.minefortress.blueprints.manager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.remmintan.mods.minefortress.core.ModLogger;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.*;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundBlueprintTaskPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.jetbrains.annotations.NotNull;
import org.minefortress.blueprints.data.ClientStructureBlockDataProvider;
import org.minefortress.interfaces.IFortressMinecraftClient;
import org.minefortress.renderer.gui.blueprints.BlueprintsScreen;
import org.minefortress.renderer.gui.blueprints.ImportExportBlueprintsScreen;
import org.minefortress.utils.ModUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

public final class ClientBlueprintManager extends BaseClientStructureManager implements IClientBlueprintManager {

    private final MinecraftClient client;
    private final ClientStructureBlockDataProvider blockDataManager = new ClientStructureBlockDataProvider();
    private final IBlueprintMetadataManager blueprintMetadataManager = new BlueprintMetadataManager();

    private IBlueprintMetadata selectedStructure;


    public ClientBlueprintManager(MinecraftClient client) {
        super(client);
        this.client = client;
    }



    @Override
    public boolean isSelecting() {
        return selectedStructure != null;
    }

    @Override
    public void handleBlueprintsImport() {
        this.success();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void handleBlueprintsExport(String name, byte[] bytes) {
        final var blueprintsFolder = ModUtils.getBlueprintsFolder();
        if(!blueprintsFolder.toFile().exists()) {
            blueprintsFolder.toFile().mkdirs();
        }
        final var path = blueprintsFolder.resolve(name);
        final var file = path.toFile();
        if(!file.exists()) {
            try {
                file.createNewFile();
                Files.write(path, bytes);
            } catch (IOException e) {
                e.printStackTrace();
                this.handleImportExportFailure();
                return;
            }
        }

        this.success();
    }

    @Override
    public void handleImportExportFailure() {
        final var currentScreen = MinecraftClient.getInstance().currentScreen;
        if(currentScreen instanceof ImportExportBlueprintsScreen iebs) {
            iebs.fail();
        }
    }

    private void success() {
        final var currentScreen = MinecraftClient.getInstance().currentScreen;
        if(currentScreen instanceof ImportExportBlueprintsScreen iebs) {
            iebs.success();
        }
    }

    @Override
    public void select(IBlueprintMetadata blueprintMetadata) {
        this.selectedStructure = blueprintMetadata;
        this.blueprintMetadataManager.select(blueprintMetadata);
    }

    @Override
    public void selectNext() {
        if(!this.isSelecting()) return;
        this.selectedStructure = blueprintMetadataManager.selectNext();
    }

    @Override
    public List<IBlueprintMetadata> getAllBlueprints(BlueprintGroup group) {
        return this.blueprintMetadataManager.getAllForGroup(group);
    }

    @Override
    public void buildCurrentStructure() {
        if(selectedStructure == null) {
            ModLogger.LOGGER.error("No structure selected in client blueprint manager");
            return;
        }
        if(super.getStructureRenderPos().isEmpty()) {
            ModLogger.LOGGER.error("No position selected in client blueprint manager");
            return;
        }

        if(!super.canBuild()) return;

//        addTaskToTasksHolder(taskId);
        final var serverboundBlueprintTaskPacket = getServerboundBlueprintTaskPacket();
        FortressClientNetworkHelper.send(FortressChannelNames.NEW_BLUEPRINT_TASK, serverboundBlueprintTaskPacket);

        if(!client.options.sprintKey.isPressed()) {
            clearStructure();
        }
    }

    @NotNull
    private ServerboundBlueprintTaskPacket getServerboundBlueprintTaskPacket() {
        final var selectedPawnsIds = CoreModUtils.getMineFortressManagersProvider().get_PawnsSelectionManager().getSelectedPawnsIds();
        return new ServerboundBlueprintTaskPacket(
                        UUID.randomUUID(),
                        selectedStructure.getId(),
                        getStructureBuildPos(),
                        selectedStructure.getRotation(),
                        getSelectedStructure().getFloorLevel(),
                        selectedPawnsIds
                );
    }

    @Override
    public void clearStructure() {
        this.selectedStructure = null;
    }

    @Override
    public IBlueprintMetadata getSelectedStructure() {
        return selectedStructure;
    }

    @Override
    public IBlueprintMetadataManager getBlueprintMetadataManager() {
        return blueprintMetadataManager;
    }

    @Override
    public void rotateSelectedStructureClockwise() {
        if(selectedStructure == null) throw new IllegalStateException("No blueprint selected");
        this.selectedStructure.rotateRight();
    }

    @Override
    public void rotateSelectedStructureCounterClockwise() {
        if(selectedStructure == null) throw new IllegalStateException("No blueprint selected");
        this.selectedStructure.rotateLeft();
    }

    @Override
    public IBlockDataProvider getBlockDataProvider() {
        return blockDataManager;
    }

    @Override
    public void add(BlueprintGroup group, String name, String file, int floorLevel, String requirementId, NbtCompound tag) {
        final IBlueprintMetadata metadata = this.blueprintMetadataManager.add(group, name, file, floorLevel, requirementId);
        blockDataManager.setBlueprint(metadata.getId(), tag);
        blockDataManager.invalidateBlueprint(metadata.getId());
    }

    @Override
    public void update(String fileName, NbtCompound tag, int newFloorLevel) {
        blueprintMetadataManager.update(fileName,newFloorLevel);

        blockDataManager.setBlueprint(fileName, tag);
        blockDataManager.invalidateBlueprint(fileName);
        if(client instanceof IFortressMinecraftClient fortressClient) {
            fortressClient.get_BlueprintRenderer().getBlueprintsModelBuilder().invalidateBlueprint(fileName);
        }
    }

    @Override
    public void remove(String filename) {
        blueprintMetadataManager.remove(filename);
        blockDataManager.removeBlueprint(filename);
        blockDataManager.invalidateBlueprint(filename);
    }

    @Override
    public void reset() {
        this.clearStructure();
        this.blueprintMetadataManager.reset();
        this.blockDataManager.reset();
        if(client instanceof IFortressMinecraftClient fortressClient) {
            try {
                fortressClient.get_BlueprintRenderer().getBlueprintsModelBuilder().reset();
            }catch (Exception ignore) {}
        }
    }

    @Override
    public void updateSlotsInBlueprintsScreen() {
        final var currentScreen = MinecraftClient.getInstance().currentScreen;
        if(currentScreen instanceof BlueprintsScreen bps) {
            bps.updateSlots();
        }
    }

}
