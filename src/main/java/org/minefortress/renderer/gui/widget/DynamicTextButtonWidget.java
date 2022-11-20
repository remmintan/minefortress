package org.minefortress.renderer.gui.widget;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import org.minefortress.renderer.gui.hud.interfaces.IHudButton;
import org.minefortress.renderer.gui.tooltip.BasicTooltipSupplier;

import java.util.Optional;
import java.util.function.Supplier;

public class DynamicTextButtonWidget extends ButtonWidget implements IHudButton {

    private final Integer anchorX, anchorY;
    private final Supplier<String> textSupplier;

    public DynamicTextButtonWidget(int anchorX, int anchorY, int width, int height, PressAction pressAction, String tooltipText, Supplier<String> textSupplier) {
        super(0, 0, width, height, new LiteralText(""), pressAction, new BasicTooltipSupplier(tooltipText));
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.textSupplier = textSupplier;
    }

    @Override
    public void tick() {
        this.setMessage(new LiteralText(textSupplier.get()));
    }

    @Override
    public void setPos(int x, int y) {
        this.x = x + anchorX;
        this.y = y + anchorY;
    }

    public int getAnchorX() {
        return Optional.ofNullable(anchorX).orElseThrow(() -> new IllegalStateException("Anchor X is null"));
    }

    public int getAnchorY() {
        return Optional.ofNullable(anchorY).orElseThrow(() -> new IllegalStateException("Anchor Y is null"));
    }
}
