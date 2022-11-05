package org.minefortress.entity;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public interface IItemUsingEntity {

    ItemStack getActiveItem();
    int getItemUseTimeLeft();
    boolean isUsingItem();
    void setCurrentHand(Hand hand);
    ItemStack getStackInHand(Hand hand);
    void setStackInHand(Hand hand, ItemStack stack);

    default void putItemInHand(Item item) {
        ItemStack stackInHand = getStackInHand(Hand.MAIN_HAND);
        if(item == null) {
            if(stackInHand != ItemStack.EMPTY)
                setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        } else {
            Item itemInHand = stackInHand.getItem();
            if(item.equals(itemInHand)) return;
            setStackInHand(Hand.MAIN_HAND, new ItemStack(item));
        }
    }

}
