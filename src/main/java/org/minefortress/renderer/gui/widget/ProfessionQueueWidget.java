package org.minefortress.renderer.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

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
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        final var itemRenderer = getItemRenderer();
        final var stack = Items.PLAYER_HEAD.getDefaultStack();
        drawContext.drawItem(stack, x, y);
        drawContext.drawTextWithShadow(getTextRenderer(), String.valueOf(amountSupplier.get()), x, y, 0xFFFFFF);

        if (mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16) {
            drawContext.drawTooltip(getTextRenderer(), Text.of("Number of pawns in queue"), mouseX, mouseY);
        }
    }
}
