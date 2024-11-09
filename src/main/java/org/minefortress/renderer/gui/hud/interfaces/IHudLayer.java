package org.minefortress.renderer.gui.hud.interfaces;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.minefortress.renderer.gui.hud.HudState;

public interface IHudLayer {

    void render(DrawContext drawContext, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta);
    boolean shouldRender(HudState hudState);
    default void onClick(double mouseX, double mouseY) {}

    default void tick() {
    }

    boolean isHovered();

}
