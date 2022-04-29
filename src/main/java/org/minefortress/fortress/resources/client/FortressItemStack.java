package org.minefortress.fortress.resources.client;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

public class FortressItemStack extends ItemStack {

    public FortressItemStack(ItemConvertible item, int count) {
        super(item, count);
    }

    @Override
    public int getMaxCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public ItemStack copy() {
        if (this.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemStack = new FortressItemStack(this.getItem(), this.getCount());
        itemStack.setCooldown(this.getCooldown());
        if (this.getNbt() != null) {
            itemStack.setNbt(this.getNbt().copy());
        }
        return itemStack;
    }
}
