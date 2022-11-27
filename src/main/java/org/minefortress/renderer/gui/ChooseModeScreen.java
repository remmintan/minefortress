package org.minefortress.renderer.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.minefortress.fortress.FortressGamemode;
import org.minefortress.utils.ModUtils;

import java.util.Objects;

public class ChooseModeScreen extends Screen {

    private final LiteralText questionText = new LiteralText("Choose a gamemode for this world");

    public  ChooseModeScreen() {
        super(new LiteralText("Choose Game Mode"));
    }

    @Override
    protected void init() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 24 - 16, 204, 20, new LiteralText("Creative"), button -> {
            ModUtils.getFortressClientManager().setGamemode(FortressGamemode.CREATIVE);
            closeMenu();
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 48 - 16, 204, 20, new LiteralText("Survival"), button -> {
            ModUtils.getFortressClientManager().setGamemode(FortressGamemode.SURVIVAL);
            closeMenu();
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(!ModUtils.getFortressClientManager().gamemodeNeedsInitialization()) Objects.requireNonNull(this.client).setScreen(null);
        super.renderBackground(matrices);
        ChooseModeScreen.drawCenteredText(matrices, this.textRenderer, questionText, this.width / 2, 40, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void closeMenu() {
        Objects.requireNonNull(this.client).setScreen(null);
    }
}
