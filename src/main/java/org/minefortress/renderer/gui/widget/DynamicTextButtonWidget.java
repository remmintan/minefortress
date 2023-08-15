package org.minefortress.renderer.gui.widget;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.minefortress.renderer.gui.hud.interfaces.IHudButton;

import java.util.function.Supplier;

public class DynamicTextButtonWidget extends ButtonWidget implements IHudButton {

    private final int anchorX, anchorY;
    private final Supplier<String> textSupplier;

    public DynamicTextButtonWidget(int anchorX, int anchorY, int width, int height, PressAction pressAction, String tooltipText, Supplier<String> textSupplier) {
        super(0, 0, width, height, Text.literal(""), pressAction, (it) -> Text.literal(tooltipText));
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.textSupplier = textSupplier;
    }

    @Override
    public void tick() {
        this.setMessage(Text.literal(textSupplier.get()));
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
