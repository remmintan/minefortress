package org.minefortress.renderer.gui.widget;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import org.minefortress.renderer.gui.hud.interfaces.IHudButton;
import org.minefortress.renderer.gui.tooltip.BasicTooltipSupplier;

public class TextButtonWidget extends ButtonWidget implements IHudButton {

    private final int anchorX;
    private final int anchorY;

    public TextButtonWidget(int anchorX, int anchorY, int width, int height, String message, PressAction onPress, String tooltipText) {
        super(0, 0, width, height, new LiteralText(message), onPress, new BasicTooltipSupplier(tooltipText));
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    @Override
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
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
