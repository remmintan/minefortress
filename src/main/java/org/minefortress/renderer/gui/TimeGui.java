package org.minefortress.renderer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.world.World;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.gui.widget.TimeButtonWidget;

import java.util.Optional;

public class TimeGui extends FortressGuiScreen {

    private final TimeButtonWidget pause;
    private final TimeButtonWidget speed1;
    private final TimeButtonWidget speed2;
//    private final TimeButtonWidget speed4;

    private final FortressMinecraftClient fortressClient;

    private static final int buttonWidth = 20;
    private static final int buttonHeight = 20;

    TimeGui(MinecraftClient client, ItemRenderer itemRenderer) {
        super(client, itemRenderer);

        this.fortressClient = (FortressMinecraftClient) client;

        pause = new TimeButtonWidget(
                0, 0, buttonWidth, buttonHeight,
                new LiteralText("||"),
                b -> this.setSpeed(0),
                () -> this.fortressClient.getTicksSpeed() == 0
        );
        speed1 = new TimeButtonWidget(
                0, 0, buttonWidth, buttonHeight,
                new LiteralText(">"),
                b -> this.setSpeed(1),
                () -> this.fortressClient.getTicksSpeed() == 1
        );
        speed2 = new TimeButtonWidget(
                0, 0, buttonWidth, buttonHeight,
                new LiteralText(">>"),
                b -> this.setSpeed(16),
                () -> this.fortressClient.getTicksSpeed() == 16
        );
//        speed4 = new TimeButtonWidget(
//                0, 0, buttonWidth, buttonHeight,
//                new LiteralText("x4"),
//                b -> this.setSpeed(64),
//                () -> this.fortressClient.getTicksSpeed() == 64
//        );
    }

    @Override
    void render(MatrixStack p, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta) {
        if(!this.isCombat()) {
            final int y = 15;

            this.pause.x = 5;
            this.pause.y = y;
            this.pause.render(p, (int)mouseX, (int)mouseY, delta);

            this.speed1.x = 5 + (5 + 20);
            this.speed1.y = y;
            this.speed1.render(p, (int)mouseX, (int)mouseY, delta);

            this.speed2.x = 5 + (5 + 20) * 2;
            this.speed2.y = y;
            this.speed2.render(p, (int)mouseX, (int)mouseY, delta);

//        this.speed4.x = 5 + (5 + 20) * 3;
//        this.speed4.y = y;
//        this.speed4.render(p, (int)mouseX, (int)mouseY, delta);
        }

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
        return this.pause.isHovered() || this.speed1.isHovered() || this.speed2.isHovered();//this.speed4.isHovered();
    }

    @Override
    void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        if(this.pause.isHovered())
            this.pause.onClick(mouseX, mouseY);

        if(this.speed1.isHovered())
            this.speed1.onClick(mouseX, mouseY);

        if(this.speed2.isHovered())
            this.speed2.onClick(mouseX, mouseY);

//        if(this.speed4.isHovered())
//            this.speed4.onClick(mouseX, mouseY);
    }

    private void setSpeed(int speed) {
        this.fortressClient.setTicksSpeed(speed);
    }

    private FortressMinecraftClient getClient() {
        return (FortressMinecraftClient) MinecraftClient.getInstance();
    }

    private boolean isCombat() {
        return getClient().getFortressClientManager().isInCombat();
    }

}
