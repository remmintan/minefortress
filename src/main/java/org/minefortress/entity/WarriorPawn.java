package org.minefortress.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WarriorPawn extends BasePawnEntity implements IWarriorPawn {

    private LivingEntity attackTarget;
    private BlockPos moveTarget;

    protected WarriorPawn(EntityType<? extends BasePawnEntity> entityType, World world) {
        super(entityType, world, false);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
    }

    @Override
    protected void initGoals() {
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, HostileEntity.class, false));
    }

    @Override
    public void setAttackTarget(@Nullable LivingEntity entity) {
        this.resetTargets();
        this.attackTarget = entity;
    }

    @Override
    public void setMoveTarget(@Nullable BlockPos pos) {
        this.resetTargets();
        this.moveTarget = pos;
    }

    @Override
    @Nullable
    public LivingEntity getAttackTarget() {
        return attackTarget;
    }

    @Override
    @Nullable
    public BlockPos getMoveTarget() {
        return moveTarget;
    }

    private void resetTargets() {
        this.moveTarget = null;
        this.attackTarget = null;
    }

}
