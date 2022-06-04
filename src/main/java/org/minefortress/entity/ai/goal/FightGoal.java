package org.minefortress.entity.ai.goal;

import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.tasks.BuildingManager;

public class FightGoal extends AbstractFortressGoal {

    private BlockPos cachedMoveTarget;
    private BlockPos correctMoveTarget;

    public FightGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        return isInCombat();
    }

    @Override
    public void start() {
        colonist.putItemInHand(Items.WOODEN_SWORD);
        colonist.setCurrentTaskDesc("Fighting");
    }


    @Override
    public void tick() {
//        colonist.addExhaustion(ACTIVE_EXHAUSTION);

        final var moveHelper = colonist.getMovementHelper();

        findMoveTarget();
        if(correctMoveTarget != null) {
            moveHelper.set(correctMoveTarget);
        }

        moveHelper.tick();
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
        return isInCombat();
    }

    @Override
    public void stop() {
        colonist.putItemInHand(null);
        this.cachedMoveTarget = null;
        this.correctMoveTarget = null;
    }

    @Override
    public boolean canStop() {
        return false;
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
