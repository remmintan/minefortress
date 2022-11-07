package org.minefortress.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.ai.controls.FighterMoveControl;
import org.minefortress.entity.ai.goal.warrior.FollowLivingEntityGoal;
import org.minefortress.entity.ai.goal.warrior.MeleeAttackGoal;
import org.minefortress.entity.ai.goal.warrior.MoveToBlockGoal;

import java.util.Optional;

public class WarriorPawn extends BasePawnEntity implements IWarriorPawn {

    private static final TrackedData<Optional<BlockPos>> MOVE_TARGET = DataTracker.registerData(WarriorPawn.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
    private static final TrackedData<Integer> ATTACK_TARGET = DataTracker.registerData(WarriorPawn.class, TrackedDataHandlerRegistry.INTEGER);

    private final FighterMoveControl moveControl;

    public WarriorPawn(EntityType<? extends WarriorPawn> entityType, World world) {
        super(entityType, world, false);
        moveControl = world instanceof ServerWorld ? new FighterMoveControl(this) : null;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(MOVE_TARGET, Optional.empty());
        dataTracker.startTracking(ATTACK_TARGET, -1);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if(getMoveTarget() != null || getAttackTarget() != null) {
            this.putItemInHand(Items.IRON_SWORD);
        }
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this));
        this.goalSelector.add(2, new MoveToBlockGoal(this));
        this.goalSelector.add(2, new FollowLivingEntityGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, HostileEntity.class, false));
    }

    @Override
    public String getClothingId() {
        return "warrior1";
    }

    @Override
    public void setAttackTarget(@Nullable LivingEntity entity) {
        this.resetTargets();
        if(entity != null) {
            dataTracker.set(ATTACK_TARGET, entity.getId());
        } else {
            dataTracker.set(ATTACK_TARGET, -1);
        }

    }

    @Override
    public void setMoveTarget(@Nullable BlockPos pos) {
        this.resetTargets();
        dataTracker.set(MOVE_TARGET, Optional.ofNullable(pos));
    }

    @Override
    @Nullable
    public LivingEntity getAttackTarget() {
        var id = dataTracker.get(ATTACK_TARGET);
        if(id == -1) {
            return null;
        }
        return (LivingEntity) world.getEntityById(id);
    }

    @Override
    @Nullable
    public BlockPos getMoveTarget() {
        return dataTracker.get(MOVE_TARGET).orElse(null);
    }

    @Override
    public FighterMoveControl getFighterMoveControl() {
        return moveControl;
    }

    private void resetTargets() {
        dataTracker.set(MOVE_TARGET, Optional.empty());
        dataTracker.set(ATTACK_TARGET, -1);
    }

}
