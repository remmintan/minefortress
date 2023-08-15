package org.minefortress.renderer.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.minefortress.renderer.gui.hud.interfaces.IItemHudElement;

public class ItemHudElement extends BasicHudElement implements IItemHudElement {

    private final ItemStack itemStack;

    private boolean hovered;

    public ItemHudElement(int anchorX, int anchorY, Item item) {
        super(anchorX, anchorY);
        this.itemStack = new ItemStack(item);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.drawItem(itemStack, x, y);
        this.hovered = mouseX >= x && mouseY >= y && mouseX < x + 16 && mouseY < y + 16;
    }

    @Override
    public boolean isHovered() {
        return hovered;
    }
}
