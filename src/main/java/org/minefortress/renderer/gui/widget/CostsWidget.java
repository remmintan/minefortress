package org.minefortress.renderer.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;

import java.util.Map;

public class CostsWidget implements Drawable, Element {

    private final int x;
    private final int y;
    private final Map<Item, Integer> costs;

    public CostsWidget(int x, int y, Map<Item, Integer> costs) {
        this.x = x;
        this.y = y;
        this.costs = costs;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        final var itemRenderer = getItemRenderer();
        int i = 0;
        for(var ent : costs.entrySet()) {
            final var stack = ent.getKey().getDefaultStack();
            final var count = ent.getValue();
            itemRenderer.renderGuiItemIcon(stack, x + i, y);
            final var countLabel = count > 1 ? count + "/100" : "";
            final var countLabelWidth = getTextRenderer().getWidth(countLabel)/2;
            itemRenderer.renderGuiItemOverlay(getTextRenderer(), stack, x + i + countLabelWidth, y, countLabel);
            i+=24 + countLabelWidth;
        }
    }

    private static ItemRenderer getItemRenderer() {
        return MinecraftClient.getInstance().getItemRenderer();
    }

    private static TextRenderer getTextRenderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

}
