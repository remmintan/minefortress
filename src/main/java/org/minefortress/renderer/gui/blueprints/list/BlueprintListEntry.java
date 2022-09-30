package org.minefortress.renderer.gui.blueprints.list;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class BlueprintListEntry extends AlwaysSelectedEntryListWidget.Entry<BlueprintListEntry> {

    private final String value;
    private final TextRenderer textRenderer;
    private final Text text;

    public BlueprintListEntry(String value, TextRenderer textRenderer) {
        this.value = value;
        this.text = new LiteralText(value);
        this.textRenderer = textRenderer;
    }


    @Override
    public Text getNarration() {
        return new LiteralText("Blueprints: " + value);
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        this.textRenderer.drawWithShadow(matrices, text, (float)(x + 2), (float)(y + 1), 0xFFFFFF);
    }

    public String getValue() {
        return value;
    }
}
