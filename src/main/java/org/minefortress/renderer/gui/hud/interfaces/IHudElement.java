package org.minefortress.renderer.gui.hud.interfaces;

import net.minecraft.client.util.math.MatrixStack;

public interface IHudElement {

    void setPos(int x, int y);
    int getAnchorX();
    int getAnchorY();
    default void setPosBasedOn(int basepointX, int basepointY) {
        setPos(basepointX + getAnchorX(), basepointY + getAnchorY());
    }
    void render(DrawContext matrices, int mouseX, int mouseY, float delta);
    default boolean shouldRender(boolean isCreative) {
        return true;
    }
    default void tick() {}
    boolean isHovered();

}
