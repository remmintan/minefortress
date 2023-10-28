package org.minefortress.entity.ai.goal.warrior;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.ITargetedPawn;

import java.util.EnumSet;

public class MoveToBlockGoal extends Goal {

    private final ITargetedPawn pawn;
    private BlockPos target;

    public MoveToBlockGoal(ITargetedPawn pawn) {
        this.pawn = pawn;
        setControls(EnumSet.of(Control.MOVE, Control.JUMP));
    }

    @Override
    public boolean canStart() {
        return hasMoveTarget() && farFromMoveTarget();
    }

    @Override
    public void start() {
        target = pawn.getMoveTarget();
        pawn.getFortressMoveControl().moveTo(target);
    }

    @Override
    public boolean shouldContinue() {
        return hasMoveTarget() && stillOnTheSameTarget() && farFromMoveTarget() && !pawn.getFortressMoveControl().isStuck();
    }

    @Override
    public boolean canStop() {
        return false;
    }

    @Override
    public void stop() {
        pawn.getFortressMoveControl().reset();
        target = null;
    }

    private boolean hasMoveTarget() {
        return pawn.getMoveTarget() != null;
    }

    private boolean stillOnTheSameTarget() {
        return target != null && target.equals(pawn.getMoveTarget());
    }

    private boolean farFromMoveTarget() {
        return !pawn.getMoveTarget().isWithinDistance(pawn.getPos(), pawn.getTargetMoveRange());
    }

}
