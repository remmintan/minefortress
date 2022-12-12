package org.minefortress.renderer.gui.widget;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;

import java.util.function.Supplier;

public class ProfessionQueueWidget extends MinefortressWidget implements Drawable, Element {

    private final int x;
    private final int y;
    private final Supplier<Integer> amountSupplier;

    public ProfessionQueueWidget(int x, int y, Supplier<Integer> amountSupplier) {
        this.x = x;
        this.y = y;
        this.amountSupplier = amountSupplier;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        final var itemRenderer = getItemRenderer();
        final var stack = Items.PLAYER_HEAD.getDefaultStack();
        itemRenderer.renderGuiItemIcon(stack, x, y);
        itemRenderer.renderGuiItemOverlay(getTextRenderer(), stack, x, y, String.valueOf(amountSupplier.get()));
    }
}
