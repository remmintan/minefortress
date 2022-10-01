package org.minefortress.renderer.gui.blueprints.list;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.minefortress.MineFortressMod;

public class BlueprintListEntry extends AlwaysSelectedEntryListWidget.Entry<BlueprintListEntry> {

    private final String value;
    private final TextRenderer textRenderer;
    private final Text text;
    private final BlueprintsListWidget widget;

    public BlueprintListEntry(String value, TextRenderer textRenderer, BlueprintsListWidget widget) {
        this.value = value;
        //remove blueprints extensions with substring
        this.text = new LiteralText(value.substring(0, value.length() - MineFortressMod.BLUEPRINTS_EXTENSION.length()));
        this.textRenderer = textRenderer;
        this.widget = widget;
    }

    @Override
    public Text getNarration() {
        return new LiteralText("Blueprints: " + value);
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        this.textRenderer.drawWithShadow(matrices, text, (float)(x + 2), (float)(y + 3), 0xFFFFFF);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.widget.setSelected(this);
        return true;
    }
}
