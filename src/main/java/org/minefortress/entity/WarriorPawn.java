package org.minefortress.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.ai.controls.FighterMoveControl;
import org.minefortress.entity.ai.goal.warrior.MoveToBlockGoal;
import org.minefortress.network.c2s.C2SFollowTargetPacket;
import org.minefortress.network.c2s.C2SMoveTargetPacket;
import org.minefortress.network.helpers.FortressClientNetworkHelper;

public class WarriorPawn extends BasePawnEntity implements IWarriorPawn {

    private final FighterMoveControl moveControl;

    private BlockPos moveTarget;
    private LivingEntity attackTarget;

    public WarriorPawn(EntityType<? extends WarriorPawn> entityType, World world) {
        super(entityType, world, false);
        moveControl = world instanceof ServerWorld ? new FighterMoveControl(this) : null;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if(getMoveTarget() != null || getAttackTarget() != null) {
            this.putItemInHand(Items.IRON_SWORD);
        } else {
            setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    @Override
    protected void initGoals() {
//        this.goalSelector.add(1, new MeleeAttackGoal(this));
        this.goalSelector.add(2, new MoveToBlockGoal(this));
//        this.goalSelector.add(2, new FollowLivingEntityGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, HostileEntity.class, false));
    }

    @Override
    public String getClothingId() {
        return "warrior1";
    }



    @Override
    public void setMoveTarget(@Nullable BlockPos pos) {
        if(pos != null) {
            if(world.isClient) {
                final var packet = new C2SMoveTargetPacket(pos, this.getId());
                FortressClientNetworkHelper.send(C2SMoveTargetPacket.CHANNEL, packet);
            } else {
                this.resetTargets();
                moveTarget = pos;
            }
        } else {
            throw new IllegalArgumentException("Move target cannot be null");
        }
    }

    @Override
    @Nullable
    public BlockPos getMoveTarget() {
        if(world.isClient) {
            throw new IllegalStateException("Cannot get move target on client");
        }
        return moveTarget;
    }

    @Override
    public void setAttackTarget(@Nullable LivingEntity entity) {
        if(entity != null) {
            if(world.isClient) {
                final var followPacket = new C2SFollowTargetPacket(entity.getId(), this.getId());
                FortressClientNetworkHelper.send(C2SFollowTargetPacket.CHANNEL, followPacket);
            } else {
                this.resetTargets();
                attackTarget = entity;
            }

        } else {
            throw new IllegalArgumentException("Attack target cannot be null");
        }
    }

    @Override
    @Nullable
    public LivingEntity getAttackTarget() {
        if(world.isClient) {
            throw new IllegalStateException("Cannot get attack target on client");
        }
        return attackTarget;
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
