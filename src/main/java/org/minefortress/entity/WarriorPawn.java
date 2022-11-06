package org.minefortress.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.ai.controls.FighterMoveControl;
import org.minefortress.entity.ai.goal.warrior.FollowLivingEntityGoal;
import org.minefortress.entity.ai.goal.warrior.MeleeAttackGoal;
import org.minefortress.entity.ai.goal.warrior.MoveToBlockGoal;

public class WarriorPawn extends BasePawnEntity implements IWarriorPawn {

    private FighterMoveControl moveControl;

    private LivingEntity attackTarget;
    private BlockPos moveTarget;

    protected WarriorPawn(EntityType<? extends BasePawnEntity> entityType, World world) {
        super(entityType, world, false);
        moveControl = new FighterMoveControl(this);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this));
        this.goalSelector.add(2, new MoveToBlockGoal(this));
        this.goalSelector.add(2, new FollowLivingEntityGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, HostileEntity.class, false));
    }

    @Override
    public void setAttackTarget(@Nullable LivingEntity entity) {
        this.resetTargets();
        this.putItemInHand(Items.IRON_SWORD);
        this.attackTarget = entity;
    }

    @Override
    public void setMoveTarget(@Nullable BlockPos pos) {
        this.resetTargets();
        this.putItemInHand(Items.IRON_SWORD);
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

    @Override
    public FighterMoveControl getFighterMoveControl() {
        return moveControl;
    }

    private void resetTargets() {
        this.moveTarget = null;
        this.attackTarget = null;
    }

}
