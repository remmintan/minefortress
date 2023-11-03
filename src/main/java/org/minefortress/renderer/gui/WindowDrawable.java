package org.minefortress.renderer.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.util.Identifier;

class WindowDrawable implements Drawable {
    private static final Identifier BACKGROUND_TEXTURE_IDENTIFIER = new Identifier("textures/gui/demo_background.png");

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    WindowDrawable(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(BACKGROUND_TEXTURE_IDENTIFIER, this.x, this.y, 0, 0, width, height);
    }
}
