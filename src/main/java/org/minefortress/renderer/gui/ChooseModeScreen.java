package org.minefortress.renderer.gui;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;

public class ChooseModeScreen extends Screen {

    private final LiteralText questionText = new LiteralText("Choose a game mode for this world?");

    protected ChooseModeScreen() {
        super(new LiteralText("Choose Game Mode"));
    }

    @Override
    protected void init() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 24 - 16, 204, 20, new LiteralText("Creative"), button -> {
            closeMenu();
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 48 - 16, 204, 20, new LiteralText("Survival"), button -> {
            closeMenu();
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);
        ChooseModeScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 40, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    private void closeMenu() {
        this.client.setScreen(null);
    }

    private FortressClientManager getClientManager() {
        return ((FortressMinecraftClient)this.client).getFortressClientManager();
    }
}
