package org.minefortress.renderer.gui.widget;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.minefortress.renderer.gui.widget.interfaces.TooltipRenderer;

import java.util.function.Supplier;

public class ProfessionQueueWidget extends MinefortressWidget implements Drawable, Element {

    private final int x;
    private final int y;
    private final Supplier<Integer> amountSupplier;
    private final TooltipRenderer tooltipRenderer;

    public ProfessionQueueWidget(int x, int y, Supplier<Integer> amountSupplier, TooltipRenderer tooltipRenderer) {
        this.x = x;
        this.y = y;
        this.amountSupplier = amountSupplier;
        this.tooltipRenderer = tooltipRenderer;
    }

    @Override
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        final var itemRenderer = getItemRenderer();
        final var stack = Items.PLAYER_HEAD.getDefaultStack();
        itemRenderer.renderGuiItemIcon(stack, x, y);
        itemRenderer.renderGuiItemOverlay(getTextRenderer(), stack, x, y, String.valueOf(amountSupplier.get()));

        if (mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16) {
            tooltipRenderer.render(matrices, Text.of("Number of pawns in queue"), mouseX, mouseY);
        }
    }
}
