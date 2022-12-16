package org.minefortress.renderer.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.utils.ModUtils;

import java.util.List;

public class CostsWidget implements Drawable, Element {

    private final int x;
    private final int y;
    private final List<ItemInfo> costs;

    public CostsWidget(int x, int y, List<ItemInfo> costs) {
        this.x = x;
        this.y = y;
        this.costs = costs;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        final var itemRenderer = getItemRenderer();
        int i = 0;
        for(var ent : costs) {
            final var stack = ent.item().getDefaultStack();
            final var amount = ent.amount();
            final var actualItemAmount = getItemAmount(stack);
            final var color = actualItemAmount >= amount ? 0xFFFFFF : 0xFF0000;
            final var countLabel = amount > 1 ? amount + "/" + actualItemAmount : "";
            final var textRenderer = getTextRenderer();
            final var countLabelWidth = textRenderer.getWidth(countLabel)/2;
            matrices.push();
            matrices.translate(0, 0, 500);
            textRenderer.drawWithShadow(matrices, countLabel, x + i + countLabelWidth - 20, y+8, color);
            matrices.pop();
            itemRenderer.renderGuiItemIcon(stack, x + i, y);
            i+=24 + countLabelWidth;
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

}
