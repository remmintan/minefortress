package org.minefortress.entity.ai.goal.warrior;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.Hand;
import org.minefortress.entity.IWarriorPawn;

import java.util.Optional;

public class MeleeAttackGoal extends Goal {

    private final IWarriorPawn pawn;

    public MeleeAttackGoal(IWarriorPawn pawn) {
        this.pawn = pawn;
    }

    @Override
    public boolean canStart() {
        return isNearTarget();
    }

    @Override
    public void tick() {
        getTarget().ifPresent(it -> {
            pawn.swingHand(Hand.MAIN_HAND);
            pawn.tryAttack(it);
        });
    }

    @Override
    public boolean shouldContinue() {
        return isNearTarget();
    }

    @Override
    public boolean canStop() {
        return true;
    }

    private boolean isNearTarget() {
        final var reachRange = pawn.getReachRange();
        return getTarget().map(it -> it.squaredDistanceTo(pawn.getPos()) < reachRange * reachRange).orElse(false);
    }

    private Optional<LivingEntity> getTarget() {
        return Optional.ofNullable(pawn.getTarget());
    }

}
