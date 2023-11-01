package org.minefortress.renderer.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.renderBackground(drawContext, mouseX, mouseY, delta);
        this.drawBackground(drawContext);
        super.render(drawContext, mouseX, mouseY, delta);
        drawContext.drawCenteredTextWithShadow(this.textRenderer, getTitle(), this.getScreenCenterX(), this.getScreenTopY() + 10, 0xFFFFFF);
    }

    @Override
    public final boolean shouldPause() {
        return false;
    }

    private void drawBackground(DrawContext drawContext) {
        drawContext.drawTexture(BACKGROUND_TEXTURE_IDENTIFIER, this.x, this.y, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
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
