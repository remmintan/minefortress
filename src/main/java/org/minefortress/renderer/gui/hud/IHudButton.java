package org.minefortress.renderer.gui.hud;

import net.minecraft.client.util.math.MatrixStack;

public interface IHudButton {

    void setPos(int x, int y);
    int getX();
    int getY();
    default void setPosBasedOn(int basepointX, int basepointY) {
        setPos(basepointX + getX(), basepointY + getY());
    }
    void render(MatrixStack matrices, int mouseX, int mouseY, float delta);
    default boolean shouldRender(boolean isCreative) {
        return true;
    }

}
