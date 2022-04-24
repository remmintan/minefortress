package org.minefortress.fortress.resources;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

public class FortressItemStack extends ItemStack {

    public FortressItemStack(ItemConvertible item, int count) {
        super(item, count);
    }

    @Override
    public int getMaxCount() {
        return 10000;
    }
}
