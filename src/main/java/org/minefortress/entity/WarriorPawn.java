package org.minefortress.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.ai.controls.FighterMoveControl;
import org.minefortress.entity.ai.goal.SelectTargetToAttackGoal;
import org.minefortress.entity.ai.goal.warrior.FollowLivingEntityGoal;
import org.minefortress.entity.ai.goal.warrior.MeleeAttackGoal;
import org.minefortress.entity.ai.goal.warrior.MoveToBlockGoal;
import org.minefortress.entity.interfaces.IWarriorPawn;
import org.minefortress.network.c2s.C2SFollowTargetPacket;
import org.minefortress.network.c2s.C2SMoveTargetPacket;
import org.minefortress.network.helpers.FortressClientNetworkHelper;

public final class WarriorPawn extends NamedPawnEntity implements IWarriorPawn {

    private final FighterMoveControl moveControl;

    private BlockPos moveTarget;
    private LivingEntity attackTarget;

    public WarriorPawn(EntityType<? extends WarriorPawn> entityType, World world) {
        super(entityType, world, false);
        moveControl = world instanceof ServerWorld ? new FighterMoveControl(this) : null;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this));
        this.goalSelector.add(2, new MoveToBlockGoal(this));
        this.goalSelector.add(2, new FollowLivingEntityGoal(this));
        this.goalSelector.add(9, new LookAtEntityGoal(this, LivingEntity.class, 4f));
        this.goalSelector.add(10, new LookAroundGoal(this));

        this.targetSelector.add(1, new SelectTargetToAttackGoal(this, this::canAttack));
    }

    private boolean canAttack(LivingEntity it) {
        return it.isAlive() && (it instanceof HostileEntity || it.equals(getAttackTarget()));
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
                final var followPacket = new C2SFollowTargetPacket(this.getId(), entity.getId());
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
