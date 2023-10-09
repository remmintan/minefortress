package org.minefortress.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.ai.controls.BaritoneMoveControl;
import net.remmintan.mods.minefortress.core.interfaces.pawns.ITargetedPawn;
import net.remmintan.mods.minefortress.networking.c2s.C2SFollowTargetPacket;
import net.remmintan.mods.minefortress.networking.c2s.C2SMoveTargetPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import net.remmintan.mods.minefortress.core.interfaces.pawns.IBaritoneMoveControl;

public abstract class TargetedPawn extends NamedPawnEntity implements ITargetedPawn {
    protected final IBaritoneMoveControl moveControl;
    private BlockPos moveTarget;
    private LivingEntity attackTarget;

    public TargetedPawn(EntityType<? extends BasePawnEntity> entityType, World world, boolean enableHunger) {
        super(entityType, world, enableHunger);
        moveControl = world instanceof ServerWorld ? new BaritoneMoveControl(this) : null;
    }

    @Override
    public void setMoveTarget(@Nullable BlockPos pos) {
        if (pos != null) {
            if (getWorld().isClient) {
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
        if (getWorld().isClient) {
            throw new IllegalStateException("Cannot get move target on client");
        }
        return moveTarget;
    }

    @Override
    public void setAttackTarget(@Nullable LivingEntity entity) {
        if (entity != null) {
            if (getWorld().isClient) {
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
        if (getWorld().isClient) {
            throw new IllegalStateException("Cannot get attack target on client");
        }
        return attackTarget;
    }

    @Override
    public IBaritoneMoveControl getFortressMoveControl() {
        return moveControl;
    }

    public void resetTargets() {
        this.moveTarget = null;
        this.attackTarget = null;
    }

    // need this dummy methods to make the obfuscation work
    @Override
    public Vec3d getPos() {
        return super.getPos();
    }

    @Override
    public int getId() {
        return super.getId();
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return super.getTarget();
    }
}
