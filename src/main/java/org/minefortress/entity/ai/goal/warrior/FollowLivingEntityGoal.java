package org.minefortress.entity.ai.goal.warrior;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.remmintan.mods.minefortress.core.interfaces.pawns.ITargetedPawn;

import java.util.EnumSet;

public class FollowLivingEntityGoal extends Goal {

    private final ITargetedPawn pawn;
    private LivingEntity target;

    public FollowLivingEntityGoal(ITargetedPawn pawn) {
        this.pawn = pawn;
        this.setControls(EnumSet.of(Control.MOVE, Control.JUMP));
    }

    @Override
    public boolean canStart() {
        return hasAttackTarget();
    }

    @Override
    public void start() {
        target = pawn.getAttackTarget();
        pawn.getFortressMoveControl().moveTo(target);
    }

    @Override
    public boolean shouldContinue() {
        return hasAttackTarget() && sameAttackTarget() && !pawn.getFortressMoveControl().isStuck();
    }

    @Override
    public void stop() {
        pawn.getFortressMoveControl().reset();
    }

    @Override
    public boolean canStop() {
        return true;
    }

    private boolean hasAttackTarget() {
        return pawn.getAttackTarget() != null;
    }

    private boolean sameAttackTarget() {
        return target != null && target.equals(pawn.getAttackTarget());
    }

}
