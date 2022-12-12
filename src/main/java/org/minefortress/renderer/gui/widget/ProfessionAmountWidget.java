package org.minefortress.renderer.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;

import java.util.function.Supplier;

public class ProfessionAmountWidget implements Drawable, Element {

    private final int x;
    private final int y;
    private final Item item;
    private final Supplier<Integer> amountSupplier;

    public ProfessionAmountWidget(int x, int y, Item item, Supplier<Integer> profAmountSupplier) {
        this.x = x;
        this.y = y;
        this.item = item;
        this.amountSupplier = profAmountSupplier;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        final var itemRenderer = getItemRenderer();
        itemRenderer.renderGuiItemIcon(item.getDefaultStack(), x, y);
        itemRenderer.renderGuiItemOverlay(getTextRenderer(), item.getDefaultStack(), x, y, String.valueOf(amountSupplier.get()));
    }

    private static ItemRenderer getItemRenderer() {
        return MinecraftClient.getInstance().getItemRenderer();
    }

    private static TextRenderer getTextRenderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

}
