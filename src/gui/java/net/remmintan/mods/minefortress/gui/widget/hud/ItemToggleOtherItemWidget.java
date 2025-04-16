package net.remmintan.mods.minefortress.gui.widget.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ItemToggleOtherItemWidget extends ItemToggleWidget {

    private final ItemStack otherState;

    public ItemToggleOtherItemWidget(int anchorX, int anchorY, Item item, PressAction clickAction, Function<ItemButtonWidget, Optional<String>> optTooltip,
                                     Supplier<Boolean> toggledSupplier, Supplier<Boolean> shouldRenderSupplier, Item otherItem) {
        super(anchorX, anchorY, item, clickAction, optTooltip, toggledSupplier, shouldRenderSupplier);
        this.otherState = new ItemStack(otherItem);
    }

    @Override
    protected void renderItem(DrawContext drawContext) {
        if(toggledSupplier.get())
            drawContext.drawItem(otherState, this.getX() + 2, this.getY() + 2);
        else
            super.renderBareItem(drawContext);

    }

}
