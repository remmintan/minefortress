package org.minefortress.entity.ai;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class MineFortressInventory extends SimpleInventory {

    public MineFortressInventory() {
        super(36);
        super.setStack(0, new ItemStack(Items.DIRT, 64));
        super.setStack(1, new ItemStack(Items.DIRT, 64));
        super.setStack(2, new ItemStack(Items.WATER_BUCKET));
    }



}
