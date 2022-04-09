package org.minefortress.renderer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.gui.widget.TimeButtonWidget;

public class TimeGui extends FortressGuiScreen {

    private final TimeButtonWidget pause;
    private final TimeButtonWidget speed1;
    private final TimeButtonWidget speed2;
    private final TimeButtonWidget speed4;

    private final FortressMinecraftClient fortressClient;

    private static final int buttonWidth = 20;
    private static int buttonHeight = 20;

    TimeGui(MinecraftClient client, ItemRenderer itemRenderer) {
        super(client, itemRenderer);

        this.fortressClient = (FortressMinecraftClient) client;

        pause = new TimeButtonWidget(
                0, 0, buttonWidth, buttonHeight,
                new LiteralText("x0"),
                b -> this.setSpeed(0),
                () -> this.fortressClient.getTicksSpeed() == 0
        );
        speed1 = new TimeButtonWidget(
                0, 0, buttonWidth, buttonHeight,
                new LiteralText("x1"),
                b -> this.setSpeed(1),
                () -> this.fortressClient.getTicksSpeed() == 1
        );
        speed2 = new TimeButtonWidget(
                0, 0, buttonWidth, buttonHeight,
                new LiteralText("x2"),
                b -> this.setSpeed(4),
                () -> this.fortressClient.getTicksSpeed() == 4
        );
        speed4 = new TimeButtonWidget(
                0, 0, buttonWidth, buttonHeight,
                new LiteralText("x4"),
                b -> this.setSpeed(16),
                () -> this.fortressClient.getTicksSpeed() == 16
        );
    }

    @Override
    void render(MatrixStack p, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta) {
        this.pause.x = 5;
        this.pause.y = 25;
        this.pause.render(p, (int)mouseX, (int)mouseY, delta);

        this.speed1.x = 5 + (5 + 20);
        this.speed1.y = 25;
        this.speed1.render(p, (int)mouseX, (int)mouseY, delta);

        this.speed2.x = 5 + (5 + 20) * 2;
        this.speed2.y = 25;
        this.speed2.render(p, (int)mouseX, (int)mouseY, delta);

        this.speed4.x = 5 + (5 + 20) * 3;
        this.speed4.y = 25;
        this.speed4.render(p, (int)mouseX, (int)mouseY, delta);
    }

    @Override
    boolean isHovered() {
        return this.pause.isHovered() || this.speed1.isHovered() || this.speed2.isHovered() || this.speed4.isHovered();
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

        if(this.speed4.isHovered())
            this.speed4.onClick(mouseX, mouseY);
    }

    private void setSpeed(int speed) {
        this.fortressClient.setTicksSpeed(speed);
    }

}
