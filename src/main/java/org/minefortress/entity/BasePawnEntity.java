package org.minefortress.entity;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.minefortress.MineFortressConstants;
import org.minefortress.entity.interfaces.IFortressAwareEntity;
import org.minefortress.interfaces.FortressSlimeEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class BasePawnEntity extends HungryEntity implements IFortressAwareEntity {

    private static final String FORTRESS_ID_KEY = "playerId";
    private static final TrackedData<Optional<UUID>> FORTRESS_ID = DataTracker.registerData(BasePawnEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    protected BasePawnEntity(EntityType<? extends BasePawnEntity> entityType, World world, boolean enableHunger) {
        super(entityType, world, enableHunger);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(FORTRESS_ID, Optional.empty());
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0d)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.15d)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 2.0d)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED)
                .add(EntityAttributes.GENERIC_LUCK);
    }

    public int getBodyTextureId() {
        return 0;
    }

    public abstract String getClothingId();

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        if(entityNbt == null) throw new IllegalStateException("Entity nbt cannot be null");
        final var fortressId = entityNbt.getUuid(MineFortressConstants.FORTRESS_ID_KEY);
        this.setFortressId(fortressId);
        addThisPawnToFortress();
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    private void addThisPawnToFortress() {
        getFortressServerManager().ifPresent(fsm -> fsm.addColonist(this));
    }

    private void setFortressId(UUID fortressId) {
        this.dataTracker.set(FORTRESS_ID, Optional.ofNullable(fortressId));
    }

    @Override
    public Optional<UUID> getFortressId() {
        return this.dataTracker.get(FORTRESS_ID);
    }

    @Override
    public final @Nullable PlayerEntity getPlayer() {
        return getMasterPlayer().orElse(null);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        this.getFortressId().ifPresent(it -> nbt.putUuid(FORTRESS_ID_KEY, it));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if(nbt.contains(FORTRESS_ID_KEY)) {
            final var fortressId = nbt.getUuid(FORTRESS_ID_KEY);
            this.setFortressId(fortressId);
            addThisPawnToFortress();
        }
    }

    @Override
    public final boolean isInvulnerableTo(DamageSource damageSource) {
        if(damageSource == DamageSource.FALL) return true;
        return super.isInvulnerableTo(damageSource);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public void tickMovement() {
        super.tickHandSwing();
        super.tickMovement();

        Box boundingBox = this.getBoundingBox();
        List<SlimeEntity> touchingSlimes = world.getEntitiesByClass(SlimeEntity.class, boundingBox, slimeEntity -> true);
        touchingSlimes.forEach(s -> ((FortressSlimeEntity)s).touchPawn(this));
    }

}
