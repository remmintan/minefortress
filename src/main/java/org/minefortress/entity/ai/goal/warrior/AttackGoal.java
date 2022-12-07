package org.minefortress.entity.ai.goal.warrior;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import org.minefortress.entity.BasePawnEntity;

import java.util.Optional;

abstract class AttackGoal extends Goal {

    protected final BasePawnEntity pawn;

    public AttackGoal(BasePawnEntity pawn) {
        this.pawn = pawn;
    }

    @Override
    public boolean canStart() {
        return getTarget().map(LivingEntity::isAlive).orElse(false);
    }

    @Override
    public boolean canStop() {
        return true;
    }

    protected Optional<LivingEntity> getTarget() {
        return Optional.ofNullable(pawn.getTarget());
    }

}
