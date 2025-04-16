package net.remmintan.mods.minefortress.gui.widget.professions;


import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.text.Text;


public class ProfessionNameWidget extends BaseProfessionWidget implements Element, Drawable {

    private static final int MAX_NAME_LENGTH = 8;
    private final String fullName;
    private final String shortName;
    private final int x;
    private final int y;
    public ProfessionNameWidget(String fullName, int x, int y) {
        this.fullName = fullName;
        this.x = x;
        this.y = y;
        var preparedName = fullName.replace(" - ", " ").replace("LVL", "");
        if (preparedName.length() > getMaxNameLength()) {
            this.shortName = preparedName.substring(0, getMaxNameLength() - 3) + "...";
        } else {
            this.shortName = preparedName;
        }
    }

    private int getMaxNameLength() {
        return MAX_NAME_LENGTH + 10;
    }

    private float getScaleFactor() {
        return 1f;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        final var scaleFactor = getScaleFactor();
        final var matrices = drawContext.getMatrices();
        matrices.push();
        matrices.scale(scaleFactor, scaleFactor, 1f);
        final var newX = (int) (x / scaleFactor);
        final var newY = (int) (y / scaleFactor);
        drawContext.drawText(getTextRenderer(), shortName, newX, newY, 0xFFFFFF, false);
        matrices.pop();
        // if hovered, draw full areaType
        if (mouseX >= x && mouseX <= x + getTextRenderer().getWidth(shortName) * scaleFactor && mouseY >= y && mouseY <= y + getTextRenderer().fontHeight * scaleFactor) {
            drawContext.drawTooltip(getTextRenderer(), Text.of(fullName), mouseX, mouseY);
        }
    }

    public int getOffset() {
        return (int) (getTextRenderer().getWidth(shortName) * getScaleFactor());
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }
}
