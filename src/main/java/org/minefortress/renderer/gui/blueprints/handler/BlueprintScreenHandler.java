package org.minefortress.renderer.gui.blueprints.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.minefortress.blueprints.manager.BlueprintMetadata;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.ServerboundEditBlueprintPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.renderer.gui.blueprints.BlueprintGroup;

import java.util.ArrayList;
import java.util.List;

public final class BlueprintScreenHandler {

    private final FortressMinecraftClient fortressClient;

    private BlueprintGroup selectedGroup = BlueprintGroup.LIVING_HOUSES;

    private List<BlueprintSlot> currentSlots;
    private boolean needScrollbar = false;
    private int totalSize = 0;

    private BlueprintSlot focusedSlot;

    public BlueprintScreenHandler(MinecraftClient client){
        if(!(client instanceof FortressMinecraftClient))
            throw new IllegalArgumentException("Client must be an instance of FortressMinecraftClient");
        this.fortressClient = (FortressMinecraftClient)client;
        this.scroll(0f);
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
        final List<BlueprintMetadata> allBlueprint = fortressClient.getBlueprintManager().getAllBlueprints(selectedGroup);
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
                    this.currentSlots.add(new BlueprintSlot(blueprintMetadata));
                }
            }
        }

        this.needScrollbar = this.totalSize > 9 * 5;
    }

    public void sendEditPacket() {
        final BlueprintMetadata metadata = this.focusedSlot.getMetadata();
        final String file = metadata.getFile();
        final int floorLevel = metadata.getFloorLevel();
        final ServerboundEditBlueprintPacket packet = new ServerboundEditBlueprintPacket(file, floorLevel);
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
        fortressClient.getBlueprintManager().select(focusedSlot.getMetadata());
    }

    public int getSelectedGroupSize() {
        return totalSize;
    }

    public Text getFocusedSlotName() {
        return this.focusedSlot.getTooltipText();
    }



}
