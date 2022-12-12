package org.minefortress.renderer.gui.widget;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;


public class ProfessionNameWidget extends MinefortressWidget implements Element, Drawable {

    private static final int MAX_NAME_LENGTH = 8;
    private final String fullName;
    private final String shortName;
    private final int x;
    private final int y;

    private final TooltipRenderer tooltipRenderer;

    public ProfessionNameWidget(String fullName, int x, int y,  TooltipRenderer tooltipRenderer) {
        this.fullName = fullName;
        this.x = x;
        this.y = y;
        if (fullName.length() > MAX_NAME_LENGTH) {
            this.shortName = fullName.substring(0, MAX_NAME_LENGTH - 1) + "...";
        } else {
            this.shortName = fullName;
        }
        this.tooltipRenderer = tooltipRenderer;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        getTextRenderer().drawWithShadow(matrices, shortName, x, y, 0xFFFFFF);
        // if hovered, draw full name
        if (mouseX >= x && mouseX <= x + getTextRenderer().getWidth(shortName) && mouseY >= y && mouseY <= y + getTextRenderer().fontHeight) {
            tooltipRenderer.render(matrices, Text.of(fullName), mouseX, mouseY);
        }
    }

    public int getOffset() {
        return getTextRenderer().getWidth(shortName);
    }

    @FunctionalInterface
    public interface TooltipRenderer {
        void render(MatrixStack matrices, Text text, int mouseX, int mouseY);
    }

}
