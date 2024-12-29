package org.minefortress.blueprints.manager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.ModLogger;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.*;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientPawnsSelectionManager;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundBlueprintTaskPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.jetbrains.annotations.NotNull;
import org.minefortress.blueprints.data.ClientStructureBlockDataProvider;
import org.minefortress.interfaces.IFortressMinecraftClient;
import org.minefortress.renderer.gui.blueprints.BlueprintsScreen;

import java.util.List;

public final class ClientBlueprintManager extends BaseClientStructureManager implements IClientBlueprintManager {

    private final MinecraftClient client;
    private final ClientStructureBlockDataProvider blockDataManager = new ClientStructureBlockDataProvider();
    private final IBlueprintMetadataManager blueprintMetadataManager = new BlueprintMetadataManager();

    private BlueprintMetadata selectedStructure;
    private IBlueprintRotation selectedRotation;
    private BlockPos upgradingBuildingPos;
    private BlockBox upgradingBuildingBox;

    public ClientBlueprintManager(MinecraftClient client) {
        super(client);
        this.client = client;
    }

    @Override
    public boolean isSelecting() {
        return selectedStructure != null;
    }

    @Override
    public void select(BlueprintMetadata blueprintMetadata) {
        this.selectedStructure = blueprintMetadata;
        this.selectedRotation = new BlueprintRotation();
        this.upgradingBuildingBox = null;
        this.upgradingBuildingPos = null;
    }

    @Override
    public void selectToUpgrade(BlueprintMetadata metadata, BlockBox buildingBox, BlockPos buildingPos) {
        this.selectedStructure = metadata;
        this.selectedRotation = new BlueprintRotation();
        this.upgradingBuildingBox = buildingBox;
        this.upgradingBuildingPos = buildingPos;
    }

    @Override
    public boolean isUpgrading() {
        return upgradingBuildingBox != null && upgradingBuildingPos != null;
    }

    @Override
    public List<BlueprintMetadata> getAllBlueprints(BlueprintGroup group) {
        return this.blueprintMetadataManager.getAllForGroup(group);
    }

    @Override
    public void buildCurrentStructure() {
        if (selectedStructure == null) {
            ModLogger.LOGGER.error("No structure selected in client blueprint manager");
            return;
        }
        if (super.getStructureRenderPos().isEmpty()) {
            ModLogger.LOGGER.error("No position selected in client blueprint manager");
            return;
        }

        if (!super.canBuild()) return;

        final var selectionManager = CoreModUtils.getManagersProvider().get_PawnsSelectionManager();
        final var serverboundBlueprintTaskPacket = getServerboundBlueprintTaskPacket(selectionManager);
        FortressClientNetworkHelper.send(FortressChannelNames.NEW_BLUEPRINT_TASK, serverboundBlueprintTaskPacket);
        if ("campfire".equals(selectedStructure.getId()))
            CoreModUtils.getFortressManager().setupFortressCenter(getStructureBuildPos());
        selectionManager.resetSelection();
        clearStructure();
    }

    @NotNull
    private ServerboundBlueprintTaskPacket getServerboundBlueprintTaskPacket(IClientPawnsSelectionManager manager) {
        final var selectedPawnsIds = manager.getSelectedPawnsIds();
        return new ServerboundBlueprintTaskPacket(
                selectedStructure.getId(),
                getStructureBuildPos(),
                selectedRotation.getRotation(),
                getSelectedStructure().getFloorLevel(),
                selectedPawnsIds,
                this.upgradingBuildingPos
        );
    }

    @Override
    public void clearStructure() {
        this.selectedStructure = null;
        this.upgradingBuildingBox = null;
        this.upgradingBuildingPos = null;
    }

    @Override
    public BlueprintMetadata getSelectedStructure() {
        return selectedStructure;
    }

    @Override
    public IBlueprintRotation getSelectedRotation() {
        return selectedRotation;
    }

    @Override
    public IBlueprintMetadataManager getBlueprintMetadataManager() {
        return blueprintMetadataManager;
    }

    @Override
    public void rotateSelectedStructureClockwise() {
        if (selectedRotation == null) throw new IllegalStateException("No blueprint selected");
        this.selectedRotation.rotateRight();
    }

    @Override
    public void rotateSelectedStructureCounterClockwise() {
        if (selectedRotation == null) throw new IllegalStateException("No blueprint selected");
        this.selectedRotation.rotateLeft();
    }

    @Override
    public IBlockDataProvider getBlockDataProvider() {
        return blockDataManager;
    }

    @Override
    public void sync(BlueprintMetadata metadata, NbtCompound tag) {
        this.blueprintMetadataManager.sync(metadata);
        blockDataManager.setBlueprint(metadata.getId(), tag);
        blockDataManager.invalidateBlueprint(metadata.getId());
        if (client instanceof IFortressMinecraftClient fortressClient) {
            fortressClient
                    .get_BlueprintRenderer()
                    .getBlueprintsModelBuilder()
                    .invalidateBlueprint(metadata.getId());
        }
    }

    @Override
    public void remove(String blueprintId) {
        blueprintMetadataManager.remove(blueprintId);
        blockDataManager.removeBlueprint(blueprintId);
        blockDataManager.invalidateBlueprint(blueprintId);
    }

    @Override
    public void reset() {
        this.clearStructure();
        this.blueprintMetadataManager.reset();
        this.blockDataManager.reset();
        if (client instanceof IFortressMinecraftClient fortressClient) {
            try {
                fortressClient.get_BlueprintRenderer().getBlueprintsModelBuilder().reset();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void updateSlotsInBlueprintsScreen() {
        final var currentScreen = MinecraftClient.getInstance().currentScreen;
        if (currentScreen instanceof BlueprintsScreen bps) {
            bps.updateSlots();
        }
    }

    BlockBox getUpgradingBuildingBox() {
        return upgradingBuildingBox;
    }
}
