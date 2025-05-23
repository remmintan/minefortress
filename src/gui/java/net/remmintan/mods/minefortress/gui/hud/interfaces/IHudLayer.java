package net.remmintan.mods.minefortress.gui.hud.interfaces;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.remmintan.mods.minefortress.gui.hud.HudState;

public interface IHudLayer {

    void render(DrawContext drawContext, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta);
    boolean shouldRender(HudState hudState);

    // Renamed from onClick to onReleaseClick for clarity, and added onHudPress
    default boolean onHudPress(double mouseX, double mouseY) {
        return false;
    } // Return true if event consumed

    default void onHudRelease(double mouseX, double mouseY) {
    }

    default void tick() {
    }

    boolean isHovered();

}