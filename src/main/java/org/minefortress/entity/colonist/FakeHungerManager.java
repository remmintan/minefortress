package org.minefortress.entity.colonist;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.HungerManager;

public class FakeHungerManager extends HungerManager implements IFortressHungerManager {

    public void update(LivingEntity livingEntity) {
        this.foodLevel = 20;
        this.saturationLevel = 5.0f;
        this.exhaustion = 0;
        this.foodTickTimer = 0;
        this.prevFoodLevel = 20;
    }

    @Override
    public HungerManager toHungerManager() {
        return this;
    }

}
