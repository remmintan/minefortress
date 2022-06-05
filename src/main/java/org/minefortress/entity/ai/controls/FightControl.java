package org.minefortress.entity.ai.controls;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.professions.ProfessionManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FightControl {

    private static final List<String> DEFENDER_PROFESSIONS = Arrays.asList(
            "warrior1",
            "warrior2",
            "colonist"
    );

    private static final List<String> WARRIOR_PROFESSIONS = Arrays.asList(
            "warrior1",
            "warrior2"
    );
    
    private static final List<String> LONG_RANGE_ATTACKERS = Collections.emptyList();
    
    private final Colonist colonist;

    private BlockPos moveTarget;
    private LivingEntity attackTarget;
    private boolean forcedToAttackCreeper;

    public FightControl(Colonist colonist) {
        this.colonist = colonist;
    }

    public void tick() {
        if(!this.attackTarget.isAlive()){
            this.attackTarget = null;
        }
        
        if(this.attackTarget == null && dontHaveMoveTarget()) {
            final var target = this.colonist.getTarget();
            if(target instanceof HostileEntity && isTargetAcceptable(target)) {
                this.attackTarget = target;
            } else {
                this.attackTarget = null;
            }
        }
    }
    
    private boolean dontHaveMoveTarget() {
        return this.moveTarget == null || this.moveTarget.isWithinDistance(this.colonist.getBlockPos().up(), Colonist.WORK_REACH_DISTANCE);
    }

    private boolean isTargetAcceptable(LivingEntity target) {
        if(target instanceof CreeperEntity) {
            return longRangeAttacker();
        }
        return true;
    }

    private boolean longRangeAttacker() {
        return LONG_RANGE_ATTACKERS.contains(colonist.getProfessionId());
    }

    public void reset() {
        moveTarget = null;
        attackTarget = null;
        this.forcedToAttackCreeper = false;
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

    public LivingEntity getAttackTarget() {
        return attackTarget;
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
}
