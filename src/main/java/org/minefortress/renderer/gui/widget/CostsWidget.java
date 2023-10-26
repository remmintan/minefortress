package org.minefortress.renderer.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.remmintan.mods.minefortress.core.interfaces.resources.IItemInfo;
import org.minefortress.utils.GuiUtils;
import org.minefortress.utils.ModUtils;

import java.util.List;

public class CostsWidget implements Drawable, Element {

    private final int x;
    private final int y;
    private final List<IItemInfo> costs;

    public CostsWidget(int x, int y, List<IItemInfo> costs) {
        this.x = x;
        this.y = y;
        this.costs = costs;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        final var itemRenderer = getItemRenderer();
        int i = 0;
        for(var ent : costs) {
            final var stack = ent.item().getDefaultStack();
            final var amount = ent.amount();
            final var actualItemAmount = getItemAmount(stack);
            final var color = actualItemAmount >= amount ? 0xFFFFFF : 0xFF0000;
            final var countLabel = amount + "/" + GuiUtils.formatSlotCount(actualItemAmount);
            final var textRenderer = getTextRenderer();
            final var countLabelWidth = textRenderer.getWidth(countLabel);
            final var matrices = drawContext.getMatrices();
            matrices.push();
            final var scaleFactor = 0.5f;
            matrices.scale(scaleFactor, scaleFactor, 1f);
            matrices.translate(30, 0, 500);

            final var textX = x + i + countLabelWidth / 2f - 25 * scaleFactor;
            final var textY = y + 6 / scaleFactor;
            drawContext.drawText(getTextRenderer(), countLabel, (int)(textX / scaleFactor), (int)(textY / scaleFactor), color, false);
            matrices.pop();
            drawContext.drawItem(stack, x + i, y);
            i+=(9 + countLabelWidth) * scaleFactor;
        }
    }

    public boolean isEnough() {
        for(var ent : costs) {
            final var stack = ent.item().getDefaultStack();
            final var amount = ent.amount();
            final var actualItemAmount = getItemAmount(stack);
            if(actualItemAmount < amount) {
                return false;
            }
        }
        return true;
    }

    private static int getItemAmount(ItemStack stack) {
        final var fortressClientManager = ModUtils.getFortressClientManager();
        return fortressClientManager.getResourceManager().getItemAmount(stack.getItem());
    }

    private static ItemRenderer getItemRenderer() {
        return MinecraftClient.getInstance().getItemRenderer();
    }

    private static TextRenderer getTextRenderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }
}
