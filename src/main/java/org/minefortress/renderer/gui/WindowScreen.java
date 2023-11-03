package org.minefortress.renderer.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class WindowScreen extends Screen {


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

        final var windowDrawable = new WindowDrawable(x, y, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        this.addDrawable(windowDrawable);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);
        drawContext.drawCenteredTextWithShadow(this.textRenderer, getTitle(), this.getScreenCenterX(), this.getScreenTopY() + 10, 0xFFFFFF);
    }

    @Override
    public final boolean shouldPause() {
        return false;
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
