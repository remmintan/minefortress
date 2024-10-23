package org.minefortress.renderer.gui.blueprints.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundEditBlueprintPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.minefortress.interfaces.IFortressMinecraftClient;

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
        final List<IBlueprintMetadata> allBlueprint = blueprintManager.getAllBlueprints(selectedGroup);
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
                    final IBlueprintMetadata blueprintMetadata = allBlueprint.get(m);
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

    public void sendRemovePacket() {
        final var packet = ServerboundEditBlueprintPacket.remove(getFocusedSlot().getMetadata().getId());
        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_EDIT_BLUEPRINT, packet);
    }

    public void sendEditPacket() {
        final IBlueprintMetadata metadata = this.focusedSlot.getMetadata();
        final var requirement = metadata.getRequirement();

        final String file = metadata.getId();
        final int floorLevel = metadata.getFloorLevel();
        final ServerboundEditBlueprintPacket packet = ServerboundEditBlueprintPacket.edit(file, floorLevel, selectedGroup);
        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_EDIT_BLUEPRINT, packet);
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

    public Text getFocusedSlotName() {
        return this.focusedSlot.getTooltipText();
    }

}
