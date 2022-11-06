package org.minefortress.entity.ai.goal.warrior;

import net.minecraft.entity.ai.goal.Goal;
import org.minefortress.entity.IWarriorPawn;

import java.util.EnumSet;

public class MoveToBlockGoal extends Goal {

    private final IWarriorPawn pawn;

    public MoveToBlockGoal(IWarriorPawn pawn) {
        this.pawn = pawn;
        setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return hasMoveTarget() && farFromMoveTarget();
    }

    @Override
    public void start() {
        pawn.getFighterMoveControl().moveTo(pawn.getMoveTarget());
    }

    @Override
    public boolean shouldContinue() {
        return hasMoveTarget() && farFromMoveTarget() && !pawn.getFighterMoveControl().isStuck();
    }

    @Override
    public boolean canStop() {
        return true;
    }

    @Override
    public void stop() {
        pawn.getFighterMoveControl().reset();
    }

    private boolean hasMoveTarget() {
        return pawn.getMoveTarget() != null;
    }

    private boolean farFromMoveTarget() {
        return !pawn.getMoveTarget().isWithinDistance(pawn.getPos(), pawn.getReachRange());
    }

}
