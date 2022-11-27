package org.minefortress.entity.colonist;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.HungerManager;

public interface IFortressHungerManager {
    float ACTIVE_EXHAUSTION = 0.1f;
    float PASSIVE_EXHAUSTION = 0.0035f;
    float IDLE_EXHAUSTION = 0.002f;

    void update(LivingEntity livingEntity);
    HungerManager toHungerManager();
}
