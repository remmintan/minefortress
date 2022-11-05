package org.minefortress.entity;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public interface IHungerAwareEntity {

    float getHealth();
    ItemStack getActiveItem();
    int getItemUseTimeLeft();
    boolean isUsingItem();
    void setCurrentHand(Hand hand);
    ItemStack getStackInHand(Hand hand);
    void setStackInHand(Hand hand, ItemStack stack);

    HungerManager getHungerManager();
    int getCurrentFoodLevel();

    default void addHunger(float hunger) {
        getHungerManager().addExhaustion(hunger);
    }

    default float getHungerMultiplier() {
        final var foodLevel = this.getHungerManager().getFoodLevel();
        if(foodLevel  < 5) return 3f;
        if(foodLevel  < 10) return 1.5f;
        return 1f;
    }

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
