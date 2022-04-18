package org.minefortress.entity.colonist;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import org.minefortress.entity.Colonist;

public class ColonistHungerManager extends HungerManager {

    public void update(Colonist livingEntity) {
        boolean bl;
        Difficulty difficulty = livingEntity.world.getDifficulty();
        this.prevFoodLevel = this.foodLevel;
        if (this.exhaustion > 4.0f) {
            this.exhaustion -= 4.0f;
            if (this.saturationLevel > 0.0f) {
                this.saturationLevel = Math.max(this.saturationLevel - 1.0f, 0.0f);
//            } else if (difficulty != Difficulty.PEACEFUL) {
            } else {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }
        if ((bl = livingEntity.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)) && this.saturationLevel > 0.0f && this.foodLevel >= 20) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 10) {
                float f = Math.min(this.saturationLevel, 6.0f);
                livingEntity.heal(f / 6.0f);
                this.addExhaustion(f);
                this.foodTickTimer = 0;
            }
        } else if (bl && this.foodLevel >= 18) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 80) {
                livingEntity.heal(1.0f);
                this.addExhaustion(6.0f);
                this.foodTickTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 80) {
//                if (livingEntity.getHealth() > 10.0f || difficulty == Difficulty.HARD || livingEntity.getHealth() > 1.0f && difficulty == Difficulty.NORMAL) {
                if (livingEntity.getHealth() > 1.0f) {
                    livingEntity.damage(DamageSource.STARVE, 1.0f);
                }
                this.foodTickTimer = 0;
            }
        } else {
            this.foodTickTimer = 0;
        }
    }
}
