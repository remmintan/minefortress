package org.minefortress.renderer.gui.blueprints.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.minefortress.blueprints.BlueprintMetadata;
import org.minefortress.blueprints.BlueprintMetadataManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.gui.blueprints.BlueprintGroup;

import java.util.ArrayList;
import java.util.List;

public final class BlueprintScreenHandler {

    private final MinecraftClient client;
    private final FortressMinecraftClient fortressClient;

    private BlueprintGroup selectedGroup = BlueprintGroup.MAIN;

    private float scroll = -1f;
    private List<BlueprintSlot> currentSlots;
    private boolean needScrollbar = false;
    private int totalSize = 0;

    private BlueprintSlot focusedSlot;

    public BlueprintScreenHandler(MinecraftClient client){
        this.client = client;
        if(!(client instanceof FortressMinecraftClient))
            throw new IllegalArgumentException("Client must be an instance of FortressMinecraftClient");
        this.fortressClient = (FortressMinecraftClient)client;
        this.scroll(0f);
    }

    public void selectGroup(BlueprintGroup group){
        if(group == null) throw new IllegalArgumentException("Group cannot be null");
        this.selectedGroup = group;
    }

    public BlueprintGroup getSelectedGroup() {
        return selectedGroup;
    }

    public void scroll(float scrollPosition) {
        if(scrollPosition == scroll) return;
        final List<BlueprintMetadata> allBlueprint = getMetadataManager().getAllBlueprint();
        this.totalSize = allBlueprint.size();
        currentSlots = new ArrayList<>();
        this.needScrollbar = this.totalSize > 9 * 5;
        this.scroll = scrollPosition;
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
        // TODO: do something
    }

    public int getSelectedGroupSize() {
        return totalSize;
    }

    public Text getFocusedSlotName() {
        return this.focusedSlot.getTooltipText();
    }

    private BlueprintMetadataManager getMetadataManager() {
        return fortressClient.getBlueprintMetadataManager();
    }



}
