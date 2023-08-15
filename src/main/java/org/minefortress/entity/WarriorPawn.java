package org.minefortress.entity;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.ai.goal.EatGoal;
import org.minefortress.entity.ai.goal.SelectTargetToAttackGoal;
import org.minefortress.entity.ai.goal.warrior.CapturePositionGoal;
import org.minefortress.entity.ai.goal.warrior.FollowLivingEntityGoal;
import org.minefortress.entity.ai.goal.warrior.MeleeAttackGoal;
import org.minefortress.entity.ai.goal.warrior.MoveToBlockGoal;
import org.minefortress.entity.interfaces.IProfessional;
import org.minefortress.entity.interfaces.IWarrior;
import org.minefortress.professions.ServerProfessionManager;

public final class WarriorPawn extends TargetedPawn implements IProfessional, IWarrior {

    private static final TrackedData<String> WARRIOR_PROFESSION_KEY = DataTracker.registerData(WarriorPawn.class, TrackedDataHandlerRegistry.STRING);
    public WarriorPawn(EntityType<? extends WarriorPawn> entityType, World world) {
        super(entityType, world, true);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(WARRIOR_PROFESSION_KEY, "warrior1");
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        if(entityNbt != null) {
            final var warriorProf = entityNbt.getString(ServerProfessionManager.PROFESSION_NBT_TAG);
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
        this.goalSelector.add(3, new EatGoal(this));
        this.goalSelector.add(3, new CapturePositionGoal(this));
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
    public void resetProfession() {
        this.damage(getWorld().getDamageSources().outOfWorld(), 40f);
    }


    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString(ServerProfessionManager.PROFESSION_NBT_TAG, getProfessionId());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(WARRIOR_PROFESSION_KEY, nbt.getString(ServerProfessionManager.PROFESSION_NBT_TAG));
    }

    @Override
    public double getAttackRange() {
        return this.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
    }

    @Override
    public int getAttackCooldown() {
        return 10;
    }
}
