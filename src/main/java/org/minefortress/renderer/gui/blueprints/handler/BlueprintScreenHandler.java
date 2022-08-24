package org.minefortress.renderer.gui.blueprints.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import org.minefortress.blueprints.manager.BlueprintMetadata;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.ServerboundEditBlueprintPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.renderer.gui.BecomePatronScreen;
import org.minefortress.renderer.gui.blueprints.BlueprintGroup;

import java.util.ArrayList;
import java.util.List;

public final class BlueprintScreenHandler {

    private final MinecraftClient client;
    private final FortressMinecraftClient fortressClient;

    private BlueprintGroup selectedGroup = BlueprintGroup.LIVING_HOUSES;

    private List<BlueprintSlot> currentSlots;
    private boolean needScrollbar = false;
    private int totalSize = 0;

    private BlueprintSlot focusedSlot;

    public BlueprintScreenHandler(MinecraftClient client){
        if(!(client instanceof FortressMinecraftClient))
            throw new IllegalArgumentException("Client must be an instance of FortressMinecraftClient");
        this.client = client;
        this.fortressClient = (FortressMinecraftClient)client;
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
        final var blueprintManager = fortressClient.getBlueprintManager();
        final var fortressClientManager = fortressClient.getFortressClientManager();
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
                    final var blockData = blueprintManager.getBlockDataManager().getBlockData(blueprintMetadata.getFile(), BlockRotation.NONE);
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

    public void sendEditPacket(Screen parentScreen) {
        final BlueprintMetadata metadata = this.focusedSlot.getMetadata();
        if(fortressClient.isSupporter() || !metadata.isPremium()) {
            final String file = metadata.getFile();
            final int floorLevel = metadata.getFloorLevel();
            final ServerboundEditBlueprintPacket packet = ServerboundEditBlueprintPacket.edit(file, floorLevel, selectedGroup);
            FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_EDIT_BLUEPRINT, packet);
        } else {
            this.client.setScreen(new BecomePatronScreen(parentScreen, "Editing this blueprint"));
        }
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
        fortressClient.getBlueprintManager().select(focusedSlot.getMetadata());
    }

    public int getSelectedGroupSize() {
        return totalSize;
    }

    public Text getFocusedSlotName() {
        return this.focusedSlot.getTooltipText();
    }

}
