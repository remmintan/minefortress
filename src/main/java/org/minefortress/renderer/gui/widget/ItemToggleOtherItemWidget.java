package org.minefortress.renderer.gui.widget;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ItemToggleOtherItemWidget extends ItemToggleWidget {

    private final ItemStack otherState;

    public ItemToggleOtherItemWidget(int anchorX, int anchorY, Item item, PressAction clickAction, Function<FortressItemButtonWidget, Optional<String>> optTooltip,
                                     Supplier<Boolean> toggledSupplier, Supplier<Boolean> shouldRenderSupplier, Item otherItem) {
        super(anchorX, anchorY, item, clickAction, optTooltip, toggledSupplier, shouldRenderSupplier);
        this.otherState = new ItemStack(otherItem);
    }

    @Override
    protected void renderItem(MatrixStack m) {
        if(toggledSupplier.get())
            itemRenderer.renderInGui(otherState, this.x + 2, this.y + 2);
        else
            super.renderBareItem();

    }

}
