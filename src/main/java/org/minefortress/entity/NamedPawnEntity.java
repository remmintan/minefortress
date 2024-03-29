package org.minefortress.entity;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

abstract class NamedPawnEntity extends BasePawnEntity {

    protected NamedPawnEntity(EntityType<? extends BasePawnEntity> entityType, World world, boolean enableHunger) {
        super(entityType, world, enableHunger);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        final var initResult = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
        this.setCustomNameIfNeeded();
        return initResult;
    }

    private void setCustomNameIfNeeded() {
        getServerFortressManager().ifPresent(it -> {
            if(!this.hasCustomName()) {
                this.setCustomName(Text.literal(it.getNameGenerator().generateRandomName()));
            }
        });
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return super.damage(source, amount);
    }
}
