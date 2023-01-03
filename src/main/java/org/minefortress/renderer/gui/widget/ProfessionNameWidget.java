package org.minefortress.renderer.gui.widget;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.minefortress.renderer.gui.widget.interfaces.TooltipRenderer;


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
        var preparedName = fullName.replace(" - ", " ").replace("LVL", "");
        if (preparedName.length() > getMaxNameLength()) {
            this.shortName = preparedName.substring(0, getMaxNameLength() - 3) + "...";
        } else {
            this.shortName = preparedName;
        }
        this.tooltipRenderer = tooltipRenderer;
    }

    private int getMaxNameLength() {
        return MAX_NAME_LENGTH + 10;
    }

    private float getScaleFactor() {
        return 0.75f;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        final var scaleFactor = getScaleFactor();
        matrices.push();
        matrices.scale(scaleFactor, scaleFactor, 1f);
        final var newX = x / scaleFactor;
        final var newY = y / scaleFactor;
        getTextRenderer().drawWithShadow(matrices, shortName, newX, newY, 0xFFFFFF);
        matrices.pop();
        // if hovered, draw full name
        if (mouseX >= x && mouseX <= x + getTextRenderer().getWidth(shortName) * scaleFactor && mouseY >= y && mouseY <= y + getTextRenderer().fontHeight * scaleFactor) {
            tooltipRenderer.render(matrices, Text.of(fullName), mouseX, mouseY);
        }
    }

    public int getOffset() {
        return (int) (getTextRenderer().getWidth(shortName) * getScaleFactor());
    }

}
