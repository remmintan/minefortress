package org.minefortress.interfaces;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;

import java.util.List;

public interface FortressSimpleInventory extends Inventory {

    List<ItemStack> get_Stacks();
    int get_OccupiedSlotWithRoomForStack(ItemStack stack);
    int index_Of(ItemStack stack);
    void populate_RecipeFinder(RecipeMatcher recipeMatcher);
    int get_ChangeCount();

}
