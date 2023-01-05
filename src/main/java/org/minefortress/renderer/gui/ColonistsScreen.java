package org.minefortress.renderer.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.network.c2s.ServerboundChangeMaxColonistsCountPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.utils.ModUtils;

public class ColonistsScreen extends Screen {

    private static final Text MANAGE_COLONISTS_COUNT_TEXT = new LiteralText("Manage Villagers");

    public ColonistsScreen() {
        super(new LiteralText("Colonists"));
    }

    @Override
    protected void init() {
        this.addDrawableChild(
            new ButtonWidget(
            this.width / 2 - 102,
            this.height / 4 + 24 + 40,
            204,
            20,
            new LiteralText("Back to game"), button -> closeMenu())
        );

        this.addDrawableChild(
            new ButtonWidget(
                this.width / 2 - 102,
                this.height / 4 + 24 + 16,
                100,
                20,
                new LiteralText("<"),
                btn -> {
                    final var packet = new ServerboundChangeMaxColonistsCountPacket(
                            ServerboundChangeMaxColonistsCountPacket.ActionType.DECREASE);
                    FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_CHANGE_MAX_COLONISTS_COUNT, packet);
                }
            )
        );
        this.addDrawableChild(
            new ButtonWidget(
                this.width / 2 + 2,
                this.height / 4 + 24 + 16,
                100,
                20,
                new LiteralText(">"),
                btn -> {
                    final var packet = new ServerboundChangeMaxColonistsCountPacket(
                            ServerboundChangeMaxColonistsCountPacket.ActionType.INCREASE);
                    FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_CHANGE_MAX_COLONISTS_COUNT, packet);
                }
            )
        );
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        ColonistsScreen.drawCenteredText(matrices, this.textRenderer, MANAGE_COLONISTS_COUNT_TEXT, this.width / 2, 40, 0xFFFFFF);
        final var countLabel = "Villagers count: " + getColonistsCount();
        final var maxCountLabel = "Max villagers count: " + (getMaxColonistsCount()==-1?"Unlimited":getMaxColonistsCount());

        ColonistsScreen.drawCenteredText(matrices, this.textRenderer, new LiteralText(countLabel), this.width / 2, 60, 0xFFFFFF);
        ColonistsScreen.drawCenteredText(matrices, this.textRenderer, new LiteralText(maxCountLabel), this.width / 2, 76, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
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