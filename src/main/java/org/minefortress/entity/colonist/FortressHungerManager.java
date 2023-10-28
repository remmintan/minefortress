package org.minefortress.entity.colonist;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.world.GameRules;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;

public class FortressHungerManager extends HungerManager implements IFortressHungerManager {

    @Override
    public void update(LivingEntity livingEntity) {
        if(livingEntity instanceof IFortressAwareEntity fae) {
            final var creative = fae.getServerFortressManager().map(IServerFortressManager::isCreative).orElse(false);
            if(creative) {
                this.foodLevel = 20;
            }
        }
        boolean naturalRegen = livingEntity.getWorld().getGameRules().getBoolean(GameRules.NATURAL_REGENERATION);
        this.prevFoodLevel = this.foodLevel;
        if (this.exhaustion > 4.0f) {
            this.exhaustion -= 4.0f;
            if (this.saturationLevel > 0.0f) {
                this.saturationLevel = Math.max(this.saturationLevel - 1.0f, 0.0f);
            } else {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }
        if (naturalRegen && this.saturationLevel > 0.0f && this.foodLevel >= 20) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 10) {
                float f = Math.min(this.saturationLevel, 6.0f);
                livingEntity.heal(f / 6.0f);
                this.addExhaustion(f);
                this.foodTickTimer = 0;
            }
        } else if (naturalRegen && this.foodLevel >= 18) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 80) {
                livingEntity.heal(1.0f);
                this.addExhaustion(6.0f);
                this.foodTickTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 80) {
                if (livingEntity.getHealth() > 1.0f) {
                    livingEntity.damage(livingEntity.getWorld().getDamageSources().starve(), 1.0f);
                }
                this.foodTickTimer = 0;
            }
        } else {
            this.foodTickTimer = 0;
        }
    }

    @Override
    public HungerManager toHungerManager() {
        return this;
    }
}
