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
    private final LiteralText loadingText = new LiteralText("Loading...");
    private boolean loading = false;

    public  ChooseModeScreen() {
        super(new LiteralText("Choose Game Mode"));
    }

    @Override
    protected void init() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 24 - 16, 204, 20, new LiteralText("Creative"), button -> {
            setLoading();
            ModUtils.getFortressClientManager().setGamemode(FortressGamemode.CREATIVE);
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 48 - 16, 204, 20, new LiteralText("Survival"), button -> {
            setLoading();
            ModUtils.getFortressClientManager().setGamemode(FortressGamemode.SURVIVAL);
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(!ModUtils.getFortressClientManager().gamemodeNeedsInitialization()) Objects.requireNonNull(this.client).setScreen(null);
        super.renderBackground(matrices);
        ChooseModeScreen.drawCenteredText(matrices, this.textRenderer, loading?loadingText:questionText, this.width / 2, 40, 0xFFFFFF);
        if(loading) return;
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

    private void setLoading() {
        this.loading = true;
    }
}
