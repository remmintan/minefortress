package org.minefortress.renderer.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.network.c2s.ServerboundChangeMaxColonistsCountPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.utils.ModUtils;

public class ColonistsScreen extends Screen {

    private static final Text MANAGE_COLONISTS_COUNT_TEXT = Text.literal("Manage Villagers");

    public ColonistsScreen() {
        super(Text.literal("Colonists"));
    }

    @Override
    protected void init() {
        final var backToMenuBtn = ButtonWidget
                .builder(Text.literal("Back to game"), button -> closeMenu())
                .dimensions(this.width / 2 - 102, this.height / 4 + 24 + 40, 204, 20)
                .build();
        this.addDrawableChild(backToMenuBtn);

        final var decreaseBtn = ButtonWidget
                .builder(Text.literal("<"), button -> {
                    final var packet = new ServerboundChangeMaxColonistsCountPacket(
                            ServerboundChangeMaxColonistsCountPacket.ActionType.DECREASE);
                    FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_CHANGE_MAX_COLONISTS_COUNT, packet);
                })
                .dimensions(this.width / 2 - 102, this.height / 4 + 24 + 16, 100, 20)
                .build();
        this.addDrawableChild(decreaseBtn);


        final var increaseBtn = ButtonWidget
                .builder(Text.literal(">"), button -> {
                    final var packet = new ServerboundChangeMaxColonistsCountPacket(
                            ServerboundChangeMaxColonistsCountPacket.ActionType.INCREASE);
                    FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_CHANGE_MAX_COLONISTS_COUNT, packet);
                })
                .dimensions(this.width / 2 + 2, this.height / 4 + 24 + 16, 100, 20)
                .build();
        this.addDrawableChild(increaseBtn);


    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);
        drawContext.drawCenteredTextWithShadow(this.textRenderer, MANAGE_COLONISTS_COUNT_TEXT, this.width / 2, 40, 0xFFFFFF);
        final var countLabel = "Villagers count: " + getColonistsCount();
        final var maxCountLabel = "Max villagers count: " + (getMaxColonistsCount()==-1?"Unlimited":getMaxColonistsCount());

        drawContext.drawCenteredTextWithShadow(this.textRenderer, Text.literal(countLabel), this.width / 2, 60, 0xFFFFFF);
        drawContext.drawCenteredTextWithShadow(this.textRenderer, Text.literal(maxCountLabel), this.width / 2, 76, 0xFFFFFF);
        super.render(drawContext, mouseX, mouseY, delta);
    }

    private void closeMenu() {
        if(this.client == null) return;
        this.client.setScreen(null);
        this.client.mouse.lockCursor();
    }

    private int getMaxColonistsCount() {
        return getFortressClientManager().getMaxColonistsCount();
    }

    private int getColonistsCount() {
        return getFortressClientManager().getTotalColonistsCount();
    }

    private FortressClientManager getFortressClientManager() {
        return ModUtils.getFortressClient().getFortressClientManager();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}