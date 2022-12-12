package org.minefortress.entity.interfaces;

import net.minecraft.entity.player.HungerManager;
import org.minefortress.entity.ai.controls.EatControl;

import java.util.Optional;

public interface IHungerAwareEntity extends IItemUsingEntity {

    float getHealth();
    HungerManager getHungerManager();
    int getCurrentFoodLevel();

    default Optional<EatControl> getEatControl() {return Optional.empty();}

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
