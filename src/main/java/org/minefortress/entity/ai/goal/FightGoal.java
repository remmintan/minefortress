package org.minefortress.entity.ai.goal;

import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.controls.FightControl;
import org.minefortress.tasks.block.info.ItemTaskBlockInfo;
import org.minefortress.utils.BlockInfoUtils;
import org.minefortress.utils.BuildingHelper;

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
        if(colonist.getMovementHelper().hasReachedWorkGoal()) {
            if(colonist.getFightControl().hasFireTarget())
                colonist.getFightControl().removeFireTarget();
        }
    }

    private void findMoveTarget() {
        final var fightControl = colonist.getFightControl();
        if(fightControl.hasMoveTarget()) {
            final var moveTarget = fightControl.getMoveTarget();
            if (!moveTarget.equals(cachedMoveTarget)) {
                cachedMoveTarget = moveTarget;
                correctMoveTarget = findCorrectTarget(moveTarget);
                colonist.resetControls();
            }
        } else if(fightControl.hasFireTarget()) {
            BlockHitResult hit = fightControl.getFireTarget();
            final var moveTarget = hit.getBlockPos();
            if (!moveTarget.equals(cachedMoveTarget)) {
                cachedMoveTarget = moveTarget;
                correctMoveTarget = findCorrectTarget(moveTarget);
                if(correctMoveTarget != null) {
                    final var flintAndSteel = Items.FLINT_AND_STEEL;
                    final var hitResult = new BlockHitResult(new Vec3d(correctMoveTarget.getX(), correctMoveTarget.getY(), correctMoveTarget.getZ()), hit.getSide(), correctMoveTarget, false);
                    final var context = BlockInfoUtils.getUseOnContext(hitResult, flintAndSteel, correctMoveTarget.offset(hitResult.getSide()), (ServerWorld) colonist.world, colonist);
                    colonist.setGoal(new ItemTaskBlockInfo(flintAndSteel, correctMoveTarget, context));
                }
            }
        } else {
            this.cachedMoveTarget = null;
            this.correctMoveTarget = null;
            colonist.resetControls();
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
        colonist.resetControls();
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
        return BuildingHelper.canStayOnBlock(colonist.world, target);
    }

}
