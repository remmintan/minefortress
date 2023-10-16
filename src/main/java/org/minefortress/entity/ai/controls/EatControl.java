package org.minefortress.entity.ai.controls;

import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IHungerAwareEntity;

public class EatControl {

    private final IHungerAwareEntity colonist;
    private Item foodInHand;
    private boolean wasUsingFoodInHand;

    public EatControl(IHungerAwareEntity colonist) {
        this.colonist = colonist;
    }

    public boolean isHungry() {
        return colonist.getCurrentFoodLevel() < 12 || (colonist.getHealth() < 20 && colonist.getCurrentFoodLevel() < 20);
    }

    public void tick() {
        if(this.foodInHand != null && wasUsingFoodInHand && colonist.getActiveItem().isEmpty() && colonist.getItemUseTimeLeft() <= 0) {
            reset();
        } else if(this.foodInHand != null && !colonist.getActiveItem().getItem().equals(foodInHand)) {
            colonist.putItemInHand(this.foodInHand);
            if(!colonist.isUsingItem()) {
                colonist.setCurrentHand(Hand.MAIN_HAND);
            }
        }
        wasUsingFoodInHand = colonist.isUsingItem();
    }

    public void reset() {
        this.foodInHand = null;
        colonist.putItemInHand(null);
        wasUsingFoodInHand = false;
    }

    public void eatFood(Item food) {
        if(!food.isFood()) throw new IllegalArgumentException("Item is not food!" + food);
        this.foodInHand = food;
    }

    public boolean isEating() {
        return this.foodInHand != null;
    }

}
