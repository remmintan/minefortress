package org.minefortress.entity.ai;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.minefortress.registries.FortressItems;

public class MineFortressInventory extends SimpleInventory {

    public MineFortressInventory() {
        super(36);
        super.setStack(0, new ItemStack(FortressItems.SCAFFOLD_OAK_PLANKS, 64));
        super.setStack(1, new ItemStack(FortressItems.SCAFFOLD_OAK_PLANKS, 64));
        super.setStack(2, new ItemStack(FortressItems.SCAFFOLD_OAK_PLANKS, 64));
        super.setStack(3, new ItemStack(Items.WATER_BUCKET));
        super.setStack(4, new ItemStack(FortressItems.SCAFFOLD_OAK_PLANKS, 64));
    }

}
