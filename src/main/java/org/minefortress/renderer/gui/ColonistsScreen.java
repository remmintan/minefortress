package org.minefortress.renderer.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientFortressManager;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundChangeMaxColonistsCountPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;

public class ColonistsScreen extends Screen {

    private static final Text MANAGE_COLONISTS_COUNT_TEXT = Text.translatable("key.minefortress.manage_colonists_count");

    public ColonistsScreen() {
        super(Text.translatable("key.minefortress.colonists_screen"));
    }

    @Override
    protected void init() {
        final var backToMenuBtn = ButtonWidget
                .builder(Text.translatable("key.minefortress.back_to_menu_btn"), button -> closeMenu())
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
        this.renderBackground(drawContext, mouseX, mouseY, delta);
        drawContext.drawCenteredTextWithShadow(this.textRenderer, MANAGE_COLONISTS_COUNT_TEXT, this.width / 2, 40, 0xFFFFFF);
        final var countLabel = Text.translatable("key.minefortress.count_label").getString() + getColonistsCount();
        final var maxCountLabel = Text.translatable("key.minefortress.max_count_label").getString() + (getMaxColonistsCount()==-1?Text.translatable("key.minefortress.unlimited"):getMaxColonistsCount());

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

    private IClientFortressManager getFortressClientManager() {
        return CoreModUtils.getMineFortressManagersProvider().get_ClientFortressManager();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
