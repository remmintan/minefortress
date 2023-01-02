package org.minefortress.renderer.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WindowScreen extends Screen {

    private static final Identifier BACKGROUND_TEXTURE_IDENTIFIER = new Identifier("textures/gui/demo_background.png");

    protected static final int BACKGROUND_WIDTH = 248;
    protected static final int BACKGROUND_HEIGHT = 166;

    protected int x;
    protected int y;

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
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void drawBackground(MatrixStack matrices) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE_IDENTIFIER);
        this.drawTexture(matrices, this.x, this.y, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
    }

    protected int getScreenCenterX() {
        return this.width / 2;
    }

    protected int getScreenTopY() {
        return this.y;
    }

    protected int getScreenLeftX() {
        return this.x;
    }

    protected int getScreenRightX() {
        return this.x + BACKGROUND_WIDTH;
    }

}
