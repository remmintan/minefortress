package org.minefortress.renderer.gui.hud;

import net.minecraft.client.util.math.MatrixStack;

public interface IHudButton {

    void setPos(int basepointX, int basepointY);
    void render(MatrixStack matrices, int mouseX, int mouseY, float delta);
    boolean shouldRender(boolean isCreative);

}
