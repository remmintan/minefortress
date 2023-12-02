package org.minefortress.renderer.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import org.minefortress.renderer.gui.hud.interfaces.IHudElement;

abstract class BasicHudElement implements IHudElement {

    private final int anchorX;
    private final int anchorY;

    protected int x;
    protected int y;

    public BasicHudElement(int anchorX, int anchorY) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    @Override
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int getAnchorX() {
        return anchorX;
    }

    @Override
    public int getAnchorY() {
        return anchorY;
    }

    protected TextRenderer textRenderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

}
