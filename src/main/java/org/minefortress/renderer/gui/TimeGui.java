package org.minefortress.renderer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;

import java.util.Optional;

public class TimeGui extends FortressGuiScreen {

    TimeGui(MinecraftClient client, ItemRenderer itemRenderer) {
        super(client, itemRenderer);
    }

    @Override
    void render(MatrixStack p, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta) {
        final Optional<ClientWorld> world = Optional.ofNullable(this.client.world);
        final long timeTicks = world.map(World::getTime).orElse(0L) + 6500L;
        long timeOfDayTicks = (world.map(World::getTimeOfDay).orElse(0L) + 6500L) % 24000L;

        final int timeDays = (int) Math.floor(timeTicks / 24000.0);
        final int timeHours = (int) Math.floor(timeOfDayTicks / 1000.0);
        timeOfDayTicks %= 1000L;
        final int timeMinutes = (int) Math.floor(timeOfDayTicks / 16.66667);

        final String timeText = String.format("Day: %d | %02d:%02d", timeDays, timeHours, timeMinutes);
        TimeGui.drawStringWithShadow(p, font, timeText, screenWidth - font.getWidth(timeText) - 5, screenHeight - 15, 0xFFFFFF);
    }

    @Override
    boolean isHovered() {
        return false;
    }

    @Override
    void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
    }

}
