package org.minefortress.entity;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.minefortress.MineFortressConstants;

import java.util.Optional;
import java.util.UUID;

public class BaseColonistEntity extends HungryColonistEntity implements IFortressAwareEntity {

    private static final TrackedData<Optional<UUID>> FORTRESS_ID = DataTracker.registerData(BaseColonistEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    protected BaseColonistEntity(EntityType<? extends BaseColonistEntity> entityType, World world, boolean enableHunger) {
        super(entityType, world, enableHunger);

        this.dataTracker.startTracking(FORTRESS_ID, Optional.empty());
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        if(entityNbt == null) throw new IllegalStateException("Entity nbt cannot be null");
        final var fortressId = entityNbt.getUuid(MineFortressConstants.FORTRESS_ID_KEY);
        this.setFortressId(fortressId);
        getFortressServerManager().ifPresent(fsm -> fsm.addColonist(this));
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
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
}
