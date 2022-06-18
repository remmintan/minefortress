package org.minefortress.entity.ai.goal;

import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.controls.FightControl;
import org.minefortress.tasks.BuildingManager;

public class FightGoal extends AbstractFortressGoal {

    private BlockPos cachedMoveTarget;
    private BlockPos correctMoveTarget;

    public FightGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        return isFighting();
    }

    @Override
    public void start() {
        colonist.setCurrentTaskDesc("Fighting");
    }

    @Override
    public void tick() {
//        colonist.addExhaustion(ACTIVE_EXHAUSTION);

        final var moveHelper = colonist.getMovementHelper();

        final var fightControl = colonist.getFightControl();
        fightControl.checkAndPutCorrectItemInHand();

        findMoveTarget();
        if(correctMoveTarget != null) {
            moveHelper.set(correctMoveTarget);
        } else {
            moveHelper.reset();
        }
        if (!fightControl.hasAttackTarget() || cachedMoveTarget == null || !cachedMoveTarget.isWithinDistance(colonist.getPos(), FightControl.DEFEND_RANGE)) {
            moveHelper.tick();
        }

        fightControl.attackTargetIfPossible();
    }

    private void findMoveTarget() {
        final var fightControl = colonist.getFightControl();
        if(fightControl.hasMoveTarget()) {
            final var moveTarget = fightControl.getMoveTarget();
            if (!moveTarget.equals(cachedMoveTarget)) {
                cachedMoveTarget = moveTarget;
                correctMoveTarget = findCorrectTarget(moveTarget);
            }
        } else {
            this.cachedMoveTarget = null;
            this.correctMoveTarget = null;
        }
    }

    @Override
    public boolean shouldContinue() {
        return isFighting() && !colonist.getFightControl().creeperNearby();
    }

    @Override
    public void stop() {
        colonist.putItemInHand(null);
        this.cachedMoveTarget = null;
        this.correctMoveTarget = null;
        colonist.getMovementHelper().reset();
    }

    @Override
    public boolean canStop() {
        return colonist.getFightControl().creeperNearby();
    }

    private BlockPos findCorrectTarget(BlockPos target) {
        for (BlockPos pos : BlockPos.iterateRandomly(colonist.world.random, 125, target, 3)) {
            if (correctMoveTarget(pos)) {
                return pos;
            }
        }
        return null;
    }

    private boolean correctMoveTarget(BlockPos target) {
        return BuildingManager.canStayOnBlock(colonist.world, target);
    }

}
