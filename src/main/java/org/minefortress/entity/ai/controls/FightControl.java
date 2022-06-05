package org.minefortress.entity.ai.controls;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;

public class FightControl {

    private final Colonist colonist;

    private BlockPos moveTarget;
    private LivingEntity attackTarget;

    public FightControl(Colonist colonist) {
        this.colonist = colonist;
    }

    public void tick() {

    }

    public void reset() {
        moveTarget = null;
        attackTarget = null;
    }

    public void setMoveTarget(BlockPos moveTarget) {
        this.reset();
        this.moveTarget = moveTarget;
    }

    public void setAttackTarget(LivingEntity attackTarget) {
        this.reset();
        this.attackTarget = attackTarget;
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

    public boolean canSeeMonster() {
        final var target = this.colonist.getTarget();
        return target instanceof HostileEntity && this.colonist.getVisibilityCache().canSee(target);
    }

}
