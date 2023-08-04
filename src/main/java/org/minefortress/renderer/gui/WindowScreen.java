package org.minefortress.renderer.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.minefortress.renderer.gui.hire.HirePawnScreen;

public class WindowScreen extends Screen {

    private static final Identifier BACKGROUND_TEXTURE_IDENTIFIER = new Identifier("textures/gui/demo_background.png");

    protected static final int BACKGROUND_WIDTH = 248;
    protected static final int BACKGROUND_HEIGHT = 166;

    private int x;
    private int y;

    protected WindowScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        this.x = this.width / 2 - BACKGROUND_WIDTH / 2;
        this.y = this.height / 2 - BACKGROUND_HEIGHT / 2 - 25;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);
        this.drawBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        HirePawnScreen.drawCenteredTextWithShadow(matrices, this.textRenderer, getTitle(), this.getScreenCenterX(), this.getScreenTopY() + 10, 0xFFFFFF);
    }

    @Override
    public final boolean shouldPause() {
        return false;
    }

    private void drawBackground(MatrixStack matrices) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE_IDENTIFIER);
        this.drawTexture(matrices, this.x, this.y, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
    }

    protected final int getScreenCenterX() {
        return this.width / 2;
    }

    protected final int getScreenTopY() {
        return this.y;
    }

    protected final int getScreenLeftX() {
        return this.x;
    }

    protected final int getScreenBottomY() {
        return this.y + BACKGROUND_HEIGHT;
    }

    protected final int getScreenRightX() {
        return this.x + BACKGROUND_WIDTH;
    }

    protected final void closeScreen() {
        if(this.client != null && this.client.player != null)
            this.client.player.closeScreen();
    }

}
