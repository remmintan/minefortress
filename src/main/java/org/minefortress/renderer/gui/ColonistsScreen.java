package org.minefortress.renderer.gui;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class ColonistsScreen extends Screen {

    private static final Text MANAGE_COLONISTS_COUNT_TEXT = new LiteralText("Manage Max Colonists Count");

    protected ColonistsScreen() {
        super(new LiteralText("Colonists"));
    }

    @Override
    protected void init() {
        this.addDrawableChild(
            new ButtonWidget(
            this.width / 2 - 102,
            this.height / 4 + 24 - 16,
            204,
            20,
            new LiteralText("Back to game"), button -> closeMenu())
        );
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        GameMenuScreen.drawCenteredText(matrices, this.textRenderer, MANAGE_COLONISTS_COUNT_TEXT, this.width / 2, 40, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    private void closeMenu() {
        if(this.client == null) return;
        this.client.setScreen(null);
        this.client.mouse.lockCursor();
    }

}
