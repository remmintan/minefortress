package org.minefortress.renderer.gui.widget;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.minefortress.renderer.gui.widget.interfaces.TooltipRenderer;

import java.util.function.Supplier;

public class ProfessionAmountWidget extends MinefortressWidget implements Drawable, Element {

    private final int x;
    private final int y;
    private final ItemStack stack;
    private final Supplier<Integer> amountSupplier;
    private final Supplier<Integer> maxAmountSupplier;
    private final TooltipRenderer tooltipRenderer;

    public ProfessionAmountWidget(int x, int y, ItemStack stack, Supplier<Integer> profAmountSupplier, Supplier<Integer> maxAmountSupplier, TooltipRenderer tooltipRenderer) {
        this.x = x;
        this.y = y;
        this.stack = stack;
        this.amountSupplier = profAmountSupplier;
        this.maxAmountSupplier = maxAmountSupplier;
        this.tooltipRenderer = tooltipRenderer;
    }

    @Override
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        final var itemRenderer = getItemRenderer();
        itemRenderer.renderGuiItemIcon(stack, x, y);
        final var amount = amountSupplier.get();
        final var total = maxAmountSupplier.get();
        itemRenderer.renderGuiItemOverlay(getTextRenderer(), stack, x, y, amount + "/" + total);

        if (isHovered(mouseX, mouseY)) {
            final var text = amount >= total ? "Build more profession related houses to hire more" : "Amount of professionals";
            tooltipRenderer.render(matrices, Text.of(text), mouseX, mouseY);
        }
    }

    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16;
    }

}
