package org.minefortress.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.tasks.BuildingManager;

public class FightGoal extends AbstractFortressGoal {

    private BlockPos cachedMoveTarget;
    private BlockPos correctMoveTarget;

    private int cooldown;


    private LivingEntity attackTarget;

    public FightGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        return isFighting();
    }

    @Override
    public void start() {
        putCorrectSwordInHand();
        colonist.setCurrentTaskDesc("Fighting");
        this.cooldown = 0;
    }

    @Override
    public void tick() {
//        colonist.addExhaustion(ACTIVE_EXHAUSTION);

        final var moveHelper = colonist.getMovementHelper();

        if (colonist.getActiveItem() == null || colonist.getActiveItem().getItem() != getCorrectItem()) {
            putCorrectSwordInHand();
        }

        findMoveTarget();
        if(correctMoveTarget != null) {
            moveHelper.set(correctMoveTarget);
        } else {
            moveHelper.reset();
        }
        moveHelper.tick();

        attackTarget = colonist.getFightControl().getAttackTarget();
        final var distanceToAttackTarget = this.colonist.squaredDistanceTo(attackTarget);
        if(attackTarget != null && colonist.getNavigation().isIdle()) {
            if(distanceToAttackTarget > this.getSquaredMaxAttackDistance(attackTarget))
                colonist.getNavigation().startMovingTo(attackTarget, 1.75);
        }
        this.attack(distanceToAttackTarget);
        this.cooldown--;
        this.cooldown = Math.max(0, this.cooldown);
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
        this.attackTarget = null;
        this.cooldown = 0;
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

    protected void attack(double squaredDistance) {
        double d = this.getSquaredMaxAttackDistance(this.attackTarget);
        if (squaredDistance <= d && this.cooldown <= 0) {
            this.resetCooldown();
            this.colonist.swingHand(Hand.MAIN_HAND);
            this.colonist.tryAttack(this.attackTarget);
        }
    }

    protected void resetCooldown() {
        this.cooldown = 10;
    }

    protected double getSquaredMaxAttackDistance(LivingEntity entity) {
        return this.colonist.getWidth() * 2.0f * (this.colonist.getWidth() * 2.0f) + entity.getWidth();
    }

    private void putCorrectSwordInHand() {
        colonist.putItemInHand(getCorrectItem());
    }

    private Item getCorrectItem() {
        if(colonist.getProfessionId().equals("warrior1"))
            return Items.STONE_SWORD;
        else if(colonist.getProfessionId().equals("warrior2"))
            return Items.IRON_SWORD;
        else
            return Items.WOODEN_SWORD;
    }
}
