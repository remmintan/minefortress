package org.minefortress.interfaces;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;

import java.util.List;

public interface FortressSimpleInventory extends Inventory {

    List<ItemStack> getStacks();
    int getOccupiedSlotWithRoomForStack(ItemStack stack);
    int indexOf(ItemStack stack);
    void populateRecipeFinder(RecipeMatcher recipeMatcher);
    int getChangeCount();

}
