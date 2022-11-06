package org.minefortress.entity.ai.goal.warrior;

import net.minecraft.entity.ai.goal.Goal;
import org.minefortress.entity.IWarriorPawn;

import java.util.EnumSet;

public class FollowLivingEntityGoal extends Goal {

    private final IWarriorPawn pawn;

    public FollowLivingEntityGoal(IWarriorPawn pawn) {
        this.pawn = pawn;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return hasAttackTarget();
    }

    @Override
    public void start() {
        pawn.getFighterMoveControl().moveTo(pawn.getAttackTarget());
    }

    @Override
    public boolean shouldContinue() {
        return hasAttackTarget() && !pawn.getFighterMoveControl().isStuck();
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

}
