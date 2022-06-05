package org.minefortress.entity.ai.controls;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FightControl {

    private static final List<String> DEFENDER_PROFESSIONS = Arrays.asList(
            "warrior1",
            "warrior2",
            "colonist",
            "archer1"
    );

    private static final List<String> WARRIOR_PROFESSIONS = Arrays.asList(
            "warrior1",
            "warrior2",
            "archer1"
    );
    
    private static final List<String> LONG_RANGE_ATTACKERS = Collections.singletonList("archer1");
    
    private final Colonist colonist;

    private BlockPos moveTarget;
    private LivingEntity attackTarget;
    private boolean forcedToAttackCreeper;

    private float meleeAttackCooldown = 0;
    private float rangedAttackCooldown = 0;
    private float longRangeAttackTicks = 0;

    public FightControl(Colonist colonist) {
        this.colonist = colonist;
    }

    public void tick() {
        if(meleeAttackCooldown > 0) {
            meleeAttackCooldown--;
        }

        if(rangedAttackCooldown > 0) {
            rangedAttackCooldown--;
        }

        if(moveTargetNotReached()) return;

        if(this.attackTarget != null && !this.attackTarget.isAlive()){
            this.attackTarget = null;
        }

        colonist.getFortressServerManager().ifPresent(it -> {
            final var serverFightManager = it.getServerFightManager();
            if(serverFightManager.hasAnyScaryMob()) {
                final var randomScaryMob = serverFightManager.getRandomScaryMob(colonist.world.random);
                if(isTargetAcceptable(randomScaryMob)) {
                    this.attackTarget = randomScaryMob;
                }
            }
        });

        if(this.attackTarget == null) {
            final var target = this.colonist.getTarget();
            if(target instanceof HostileEntity && isTargetAcceptable(target)) {
                this.attackTarget = target;
            } else {
                this.attackTarget = null;
            }
        }
    }

    public void attackTargetIfPossible() {
        if(!this.hasAttackTarget()) return;
        if(!isLongRangeAttacker())
            this.meleeAttack();
        else
            this.longRangeAttack();
    }

    private void longRangeAttack() {
        final var distanceToAttackTarget = this.colonist.squaredDistanceTo(attackTarget);
        final var maxDistance = this.getSquaredMaxAttackDistance();
        if(distanceToAttackTarget > maxDistance || !this.canSeeTarget()) {
            if(colonist.getNavigation().isIdle())
                colonist.getNavigation().startMovingTo(attackTarget, 1.75);
            if(longRangeAttackTicks > 0) {
                longRangeAttackTicks--;
            }
        } else {
            colonist.getNavigation().stop();
            tickLongRangeAttack();
            this.longRangeAttackTicks++;
        }

        if(longRangeAttackTicks <= 0) {
            colonist.clearActiveItem();
        }
    }

    private void tickLongRangeAttack() {
        if(rangedAttackCooldown > 0) return;

        if(!colonist.isUsingItem()) {
            colonist.setCurrentHand(Hand.MAIN_HAND);
        }

        colonist.getLookControl().lookAt(attackTarget);

        final var itemUseTime = colonist.getItemUseTime();
        if(colonist.isUsingItem() && itemUseTime >= 20) {
            colonist.attack(attackTarget, BowItem.getPullProgress(itemUseTime));
            colonist.clearActiveItem();
            this.rangedAttackCooldown = 20;
        }
    }

    private boolean canSeeTarget() {
        return this.colonist.getVisibilityCache().canSee(this.attackTarget);
    }

    private void meleeAttack() {
        final var distanceToAttackTarget = this.colonist.squaredDistanceTo(attackTarget);
        if(distanceToAttackTarget > this.getSquaredMaxAttackDistance())
            if(colonist.getNavigation().isIdle())
                colonist.getNavigation().startMovingTo(attackTarget, 1.75);

        this.meleeAttack(distanceToAttackTarget);
    }

    private void meleeAttack(double squaredDistance) {
        double d = this.getSquaredMaxAttackDistance();
        if (squaredDistance <= d && this.meleeAttackCooldown <= 0) {
            this.meleeAttackCooldown = 20;
            this.colonist.swingHand(Hand.MAIN_HAND);
            this.colonist.tryAttack(this.attackTarget);
        }
    }

    private double getSquaredMaxAttackDistance() {
        if(isLongRangeAttacker()) {
            return 16d*16d;
        } else {
            return this.colonist.getWidth() * 2.0f * (this.colonist.getWidth() * 2.0f) + attackTarget.getWidth();
        }
    }
    
    private boolean moveTargetNotReached() {
        return this.moveTarget != null && !this.moveTarget.isWithinDistance(this.colonist.getBlockPos().up(), Colonist.WORK_REACH_DISTANCE);
    }

    private boolean isTargetAcceptable(LivingEntity target) {
        if(target instanceof CreeperEntity) {
            return isLongRangeAttacker();
        }
        return true;
    }

    private boolean isLongRangeAttacker() {
        return LONG_RANGE_ATTACKERS.contains(colonist.getProfessionId());
    }

    public void reset() {
        moveTarget = null;
        attackTarget = null;
        this.forcedToAttackCreeper = false;
        this.meleeAttackCooldown = 0;
        this.longRangeAttackTicks = 0;
        this.rangedAttackCooldown = 0;
    }

    public void setMoveTarget(BlockPos moveTarget) {
        this.reset();
        this.moveTarget = moveTarget;
    }

    public void setAttackTarget(LivingEntity attackTarget) {
        this.reset();
        this.attackTarget = attackTarget;
        this.forcedToAttackCreeper = this.attackTarget instanceof CreeperEntity;
    }

    public BlockPos getMoveTarget() {
        return moveTarget;
    }

    public boolean hasMoveTarget() {
        return moveTarget != null;
    }

    public boolean hasAttackTarget() {
        return attackTarget != null;
    }

    public boolean isDefender() {
        return DEFENDER_PROFESSIONS.contains(colonist.getProfessionId());
    }

    public boolean isWarrior() {
        return WARRIOR_PROFESSIONS.contains(colonist.getProfessionId());
    }
    
    public boolean canSeeMonster() {
        final var target = this.colonist.getTarget();
        return target instanceof HostileEntity && target.isAlive();
    }

    public boolean creeperNearby() {
        final var distance = 4;
        final var predicate = TargetPredicate.createAttackable().setBaseMaxDistance(distance).setPredicate(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR::test);
        return this.colonist.world.getClosestEntity(this.colonist.world.getEntitiesByClass(CreeperEntity.class, this.colonist.getBoundingBox().expand(distance, 3.0, distance), livingEntity -> true), predicate, this.colonist, this.colonist.getX(), this.colonist.getY(), this.colonist.getZ()) != null;
    }

    public boolean isForcedToAttackCreeper() {
        return forcedToAttackCreeper;
    }

    public static boolean isDefender(Colonist colonist) {
        return DEFENDER_PROFESSIONS.contains(colonist.getProfessionId());
    }

    public void checkAndPutCorrectItemInHand() {
        if (colonist.getActiveItem() == null || colonist.getActiveItem().getItem() != getCorrectItem()) {
            putCorrectSwordInHand();
        }
    }

    private void putCorrectSwordInHand() {
        colonist.putItemInHand(getCorrectItem());
    }

    private Item getCorrectItem() {
        if(colonist.getProfessionId().equals("warrior1"))
            return Items.STONE_SWORD;
        else if(colonist.getProfessionId().equals("warrior2"))
            return Items.IRON_SWORD;
        else if(colonist.getProfessionId().startsWith("archer"))
            return Items.BOW;
        else
            return Items.WOODEN_SWORD;
    }


}
