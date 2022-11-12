package org.minefortress.entity.ai.goal.warrior;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import org.minefortress.entity.IWarriorPawn;

import java.util.EnumSet;

public class FollowLivingEntityGoal extends Goal {

    private final IWarriorPawn pawn;
    private LivingEntity target;

    public FollowLivingEntityGoal(IWarriorPawn pawn) {
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
        pawn.getFighterMoveControl().moveTo(target);
    }

    @Override
    public boolean shouldContinue() {
        return hasAttackTarget() && sameAttackTarget() && !pawn.getFighterMoveControl().isStuck();
    }

    @Override
    public void stop() {
        pawn.getFighterMoveControl().reset();
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
