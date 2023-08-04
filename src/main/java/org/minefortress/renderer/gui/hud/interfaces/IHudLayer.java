package org.minefortress.renderer.gui.hud.interfaces;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.minefortress.renderer.gui.hud.HudState;

public interface IHudLayer {

    boolean shouldRender(HudState hudState);
    void render(DrawContext p, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta);
    default void onClick(double mouseX, double mouseY) {}
    default void tick() {};
    boolean isHovered();

}
