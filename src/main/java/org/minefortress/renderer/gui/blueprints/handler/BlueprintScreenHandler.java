package org.minefortress.renderer.gui.blueprints.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.remmintan.mods.minefortress.core.dtos.blueprints.BlueprintSlot;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundEditBlueprintPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.minefortress.interfaces.IFortressMinecraftClient;
import org.minefortress.renderer.gui.blueprints.EditUpgradesScreen;

import java.util.ArrayList;
import java.util.List;

public final class BlueprintScreenHandler {

    private final IClientManagersProvider managersProvider;

    private BlueprintGroup selectedGroup = BlueprintGroup.WORKSHOPS;

    private List<BlueprintSlot> currentSlots;
    private boolean needScrollbar = false;
    private int totalSize = 0;

    private BlueprintSlot focusedSlot;

    public BlueprintScreenHandler(MinecraftClient client){
        if(!(client instanceof IFortressMinecraftClient))
            throw new IllegalArgumentException("Client must be an instance of FortressMinecraftClient");
        this.managersProvider = (IClientManagersProvider)client;
        this.scroll(0f);
    }

    public BlueprintSlot getFocusedSlot() {
        return focusedSlot;
    }

    public void selectGroup(BlueprintGroup group){
        if(group == null) throw new IllegalArgumentException("Group cannot be null");
        this.selectedGroup = group;
        this.scroll(0f);
    }

    public BlueprintGroup getSelectedGroup() {
        return selectedGroup;
    }

    public void scroll(float scrollPosition) {
        final var blueprintManager = managersProvider.get_BlueprintManager();
        final var fortressClientManager = managersProvider.get_ClientFortressManager();
        final var resourceManager = fortressClientManager.getResourceManager();
        final List<BlueprintMetadata> allBlueprint = blueprintManager.getAllBlueprints(selectedGroup);
        this.totalSize = allBlueprint.size();
        this.currentSlots = new ArrayList<>();

        int totalRows = (this.totalSize + 9 - 1) / 9 - 5;
        int skippedRows = (int)((double)(scrollPosition * (float)totalRows) + 0.5);
        if (skippedRows < 0) {
            skippedRows = 0;
        }
        for (int row = 0; row < 5; ++row) {
            for (int column = 0; column < 9; ++column) {
                int m = column + (row + skippedRows) * 9;
                if (m >= 0 && m < this.totalSize) {
                    final BlueprintMetadata blueprintMetadata = allBlueprint.get(m);
                    final var blockData = blueprintManager.getBlockDataProvider().getBlockData(blueprintMetadata.getId(), BlockRotation.NONE);
                    if(fortressClientManager.isSurvival()) {
                        final var stacks = blockData.getStacks();
                        final var hasEnoughItems = resourceManager.hasItems(stacks);
                        this.currentSlots.add(new BlueprintSlot(blueprintMetadata, hasEnoughItems, blockData));
                    } else {
                        this.currentSlots.add(new BlueprintSlot(blueprintMetadata, true, blockData));
                    }
                }
            }
        }

        this.needScrollbar = this.totalSize > 9 * 5;
    }

    public void edit() {
        final BlueprintMetadata metadata = this.focusedSlot.getMetadata();
        final String blueprintId = metadata.getId();
        if (metadata.getRequirement().getTotalLevels() > 1) {
            final var upgrades = metadata.getRequirement().getUpgrades();
            final var ids = new ArrayList<String>();
            ids.add(blueprintId);
            ids.addAll(upgrades);
            this.openEditUpgradesScreen(ids);
        } else {
            sendEditPacket(metadata);

        }
    }

    private void sendEditPacket(BlueprintMetadata metadata) {
        final int floorLevel = metadata.getFloorLevel();
        final ServerboundEditBlueprintPacket packet = ServerboundEditBlueprintPacket.edit(metadata.getId(), floorLevel, selectedGroup);
        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_EDIT_BLUEPRINT, packet);
        MinecraftClient.getInstance().setScreen(null);
    }

    private void openEditUpgradesScreen(List<String> blueprintIdsList) {
        final var handler = new EditUpgradesScreenHandler(blueprintIdsList, this::sendEditPacket);
        final var screen = new EditUpgradesScreen(handler);
        MinecraftClient.getInstance().setScreen(screen);
    }

    public void focusOnSlot(BlueprintSlot slot) {
        this.focusedSlot = slot;
    }

    public boolean hasFocusedSlot() {
        return focusedSlot != null;
    }

    public List<BlueprintSlot> getCurrentSlots() {
        return currentSlots;
    }

    public boolean isNeedScrollbar() {
        return needScrollbar;
    }

    public void clickOnFocusedSlot() {
        if(focusedSlot == null) return;
        if(!focusedSlot.isEnoughResources()){
            final var player = MinecraftClient.getInstance().player;
            if(player != null) {
                player.sendMessage(Text.of("§cYou don't have enough resources to build this blueprint"), true);
            }
            return;
        }
        managersProvider.get_BlueprintManager().select(focusedSlot.getMetadata());
    }

    public int getSelectedGroupSize() {
        return totalSize;
    }

}
