package org.minefortress.entity.ai.controls;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;

import java.util.Optional;

public class EatControl {

    private final Colonist colonist;
    private Item foodInHand;
    private boolean wasUsingFoodInHand;

    public EatControl(Colonist colonist) {
        this.colonist = colonist;
    }

    public boolean isHungryEnough() {
        return colonist.getCurrentFoodLevel() < 12 || (colonist.getHealth() <= 10 && colonist.getCurrentFoodLevel() < 20);
    }

    public void tick() {
        if(this.foodInHand != null && wasUsingFoodInHand && colonist.getActiveItem().isEmpty() && colonist.getItemUseTimeLeft() <= 0) {
            reset();
        } else if(this.foodInHand != null && !colonist.getActiveItem().getItem().equals(foodInHand)) {
            this.colonist.setCurrentTaskDesc("Eating...");
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

    public void putFoodInHand() {
        this.getEatableItem().ifPresent(item -> {
            final var fortressServerManager = colonist.getFortressServerManager();
            if(!fortressServerManager.isVillageUnderAttack())
                fortressServerManager.getServerResourceManager().increaseItemAmount(item.getItem(), -1);
            this.foodInHand = item.getItem();
        });
    }

    private Optional<ItemStack> getEatableItem() {
        return colonist.getFortressServerManager()
                .getServerResourceManager()
                .getAllItems()
                .stream()
                .filter(this::isEatableItem)
                .findFirst();
    }

    public boolean hasEatableItem() {
        return getEatableItem().isPresent();
    }

    public boolean isEating() {
        return this.foodInHand != null;
    }

    private boolean isEatableItem(ItemStack st) {
        return !st.isEmpty() && st.getItem().isFood();
    }

}
