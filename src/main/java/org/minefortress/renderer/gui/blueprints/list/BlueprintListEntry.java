package org.minefortress.renderer.gui.blueprints.list;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
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
        this.text = Text.literal(value.substring(0, value.length() - MineFortressMod.BLUEPRINTS_EXTENSION.length()));
        this.textRenderer = textRenderer;
        this.widget = widget;
    }

    @Override
    public Text getNarration() {
        return Text.literal("Blueprints: " + value);
    }

    @Override
    public void render(DrawContext drawContext, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        drawContext.drawTextWithShadow(this.textRenderer, text, x + 2, y + 3, 0xFFFFFF);
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
