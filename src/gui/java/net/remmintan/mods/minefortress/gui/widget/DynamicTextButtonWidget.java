package net.remmintan.mods.minefortress.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.gui.hud.interfaces.IHudButton;

import java.util.function.Supplier;

public class DynamicTextButtonWidget extends ButtonWidget implements IHudButton {

    private final int anchorX, anchorY;
    private final Supplier<String> textSupplier;

    public DynamicTextButtonWidget(int anchorX, int anchorY, int width, int height, PressAction pressAction, String tooltipText, Supplier<String> textSupplier) {
        super(0, 0, width, height, Text.literal(""), pressAction, null);
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.textSupplier = textSupplier;
        this.setTooltip(Tooltip.of(Text.literal(tooltipText)));
    }

    @Override
    public void tick() {
        this.setMessage(Text.literal(textSupplier.get()));
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
    public void setPos(int x, int y) {
        this.setX(x);
        this.setY(y);
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
