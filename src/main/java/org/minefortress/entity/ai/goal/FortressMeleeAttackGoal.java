package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import org.minefortress.entity.Colonist;

public class FortressMeleeAttackGoal extends MeleeAttackGoal {

    private final Colonist colonist;

    public FortressMeleeAttackGoal(Colonist mob, double speed, boolean pauseWhenMobIdle) {
        super(mob, speed, pauseWhenMobIdle);
        this.colonist = mob;
    }

    @Override
    public boolean canStart() {
        return super.canStart() && this.colonist.getCurrentFoodLevel() > 0;
    }

    @Override
    public boolean shouldContinue() {
        return super.shouldContinue() && this.colonist.getCurrentFoodLevel() > 0;
    }
}
