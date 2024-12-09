package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.gui.hud.HudState;

import java.util.Optional;

class TimeHudLayer extends AbstractHudLayer {

    TimeHudLayer(MinecraftClient client) {
        super(client);
        this.setBasepoint(0, 0, PositionX.LEFT, PositionY.TOP);
    }

    @Override
    protected void renderHud(DrawContext drawContext, int screenWidth, int screenHeight) {
        final Optional<ClientWorld> world = Optional.ofNullable(this.client.world);
        final long timeTicks = world.map(World::getTime).orElse(0L) + 6500L;
        long timeOfDayTicks = (world.map(World::getTimeOfDay).orElse(0L) + 6500L) % 24000L;

        final int timeDays = (int) Math.floor(timeTicks / 24000.0);
        final int timeHours = (int) Math.floor(timeOfDayTicks / 1000.0);
        timeOfDayTicks %= 1000L;
        final int timeMinutes = (int) Math.floor(timeOfDayTicks / 16.66667);

        final String timeText = String.format("Day: %d | %02d:%02d", timeDays, timeHours, timeMinutes);
        drawContext.drawTextWithShadow(textRenderer, timeText, screenWidth - textRenderer.getWidth(timeText) - 5, screenHeight - 15, 0xFFFFFF);
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState != HudState.INITIALIZING;
    }
}
