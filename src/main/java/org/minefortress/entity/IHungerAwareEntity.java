package org.minefortress.entity;

import net.minecraft.entity.player.HungerManager;

public interface IHungerAwareEntity extends IItemUsingEntity {

    float getHealth();
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

}
