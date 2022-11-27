package org.minefortress.entity;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.ai.controls.BaritoneMoveControl;
import org.minefortress.entity.ai.goal.SelectTargetToAttackGoal;
import org.minefortress.entity.ai.goal.warrior.FollowLivingEntityGoal;
import org.minefortress.entity.ai.goal.warrior.MeleeAttackGoal;
import org.minefortress.entity.ai.goal.warrior.MoveToBlockGoal;
import org.minefortress.entity.interfaces.IWarriorPawn;
import org.minefortress.network.c2s.C2SFollowTargetPacket;
import org.minefortress.network.c2s.C2SMoveTargetPacket;
import org.minefortress.network.helpers.FortressClientNetworkHelper;

public final class WarriorPawn extends NamedPawnEntity implements IWarriorPawn {

    private static final TrackedData<String> WARRIOR_PROFESSION_KEY = DataTracker.registerData(WarriorPawn.class, TrackedDataHandlerRegistry.STRING);

    public static final String WARRIOR_PROFESSION_NBT_TAG = "professionId";

    private final BaritoneMoveControl moveControl;

    private BlockPos moveTarget;
    private LivingEntity attackTarget;

    public WarriorPawn(EntityType<? extends WarriorPawn> entityType, World world) {
        super(entityType, world, false);
        moveControl = world instanceof ServerWorld ? new BaritoneMoveControl(this) : null;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(WARRIOR_PROFESSION_KEY, "warrior1");
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        if(entityNbt != null) {
            final var warriorProf = entityNbt.getString(WARRIOR_PROFESSION_NBT_TAG);
            this.dataTracker.set(WARRIOR_PROFESSION_KEY, warriorProf);
        }
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
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
    public String getProfessionId() {
        return dataTracker.get(WARRIOR_PROFESSION_KEY);
    }

    @Override
    public String getClothingId() {
        return getProfessionId();
    }

    @Override
    public void resetProfession() {
        this.damage(DamageSource.OUT_OF_WORLD, 40f);
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
    public BaritoneMoveControl getFortressMoveControl() {
        return moveControl;
    }

    private void resetTargets() {
        this.moveTarget = null;
        this.attackTarget = null;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString(WARRIOR_PROFESSION_NBT_TAG, getProfessionId());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(WARRIOR_PROFESSION_KEY, nbt.getString(WARRIOR_PROFESSION_NBT_TAG));
    }
}
