package org.minefortress.renderer.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.minefortress.renderer.gui.hud.interfaces.IItemHudElement;

import java.util.Optional;
import java.util.function.Supplier;

public class ItemHudElement extends BasicHudElement implements IItemHudElement {

    private final ItemStack itemStack;
    private final Supplier<String> textSupplier;
    private boolean hovered;

    public ItemHudElement(int anchorX, int anchorY, Item item) {
        this(anchorX, anchorY, item, null);
    }

    public ItemHudElement(int anchorX, int anchorY, Item item, Supplier<String> textSupplier) {
        super(anchorX, anchorY);
        this.itemStack = new ItemStack(item);
        this.textSupplier = textSupplier;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.drawItem(itemStack, x, y);
        getCurrentText().ifPresent(it ->
                drawContext.drawText(textRenderer(), it, x + 17, y + 20, 0xFFFFFF, false));

        this.hovered = mouseX >= x && mouseY >= y && mouseX < x + 16 && mouseY < y + 16;
    }

    @Override
    public boolean isHovered() {
        return hovered;
    }

    private Optional<Text> getCurrentText() {
        return Optional.ofNullable(textSupplier).map(Supplier::get).filter(String::isBlank).map(Text::literal);
    }
}
