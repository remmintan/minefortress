package org.minefortress.renderer.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.minefortress.renderer.gui.hud.interfaces.IHudButton;

public class TextButtonWidget extends ButtonWidget implements IHudButton {

    private final int anchorX;
    private final int anchorY;

    public TextButtonWidget(int anchorX, int anchorY, int width, int height, String message, PressAction onPress, String tooltipText) {
        super(0, 0, width, height, Text.literal(message), onPress, null);
        this.setTooltip(Tooltip.of(Text.literal(tooltipText)));
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    @Override
    public void setPos(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        final var tooltip = this.getTooltip();
        if(tooltip != null && this.isHovered()) {
            final var client = MinecraftClient.getInstance();
            final var lines = tooltip.getLines(client);
            context.drawTooltip(client.textRenderer, lines, this.getTooltipPositioner(), mouseX, mouseY);
        }
    }

    @Override
    public int getAnchorX() {
        return anchorX;
    }

    @Override
    public int getAnchorY() {
        return anchorY;
    }
}
