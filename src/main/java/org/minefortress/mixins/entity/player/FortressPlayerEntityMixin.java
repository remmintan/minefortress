package org.minefortress.mixins.entity.player;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.entities.player.IFortressPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class FortressPlayerEntityMixin extends LivingEntity implements IFortressPlayerEntity {

    @Unique
    private static final String CUSTOM_NBT_KEY = "FORTRESS_PLAYER_ENTITY_CUSTOM_DATA";

    @Unique
    private static final TrackedData<Optional<BlockPos>> FORTRESS_POS = DataTracker.registerData(FortressPlayerEntityMixin.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);

    protected FortressPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTracker(CallbackInfo ci) {
        this.dataTracker.startTracking(FORTRESS_POS, Optional.empty());
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        final NbtCompound fortressPlayerEntity = new NbtCompound();

        get_FortressPos().map(BlockPos::asLong).ifPresent(it -> fortressPlayerEntity.putLong("fortressPos", it));

        nbt.put(CUSTOM_NBT_KEY, fortressPlayerEntity);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(CUSTOM_NBT_KEY)) {
            NbtCompound fortressPlayerEntity = nbt.getCompound(CUSTOM_NBT_KEY);
            if (fortressPlayerEntity.contains("fortressPos")) {
                long fortressPosLong = fortressPlayerEntity.getLong("fortressPos");
                this.set_FortressPos(BlockPos.fromLong(fortressPosLong));
            }
        }
    }

    @Override
    public @NotNull Optional<BlockPos> get_FortressPos() {
        return this.dataTracker.get(FORTRESS_POS);
    }

    @Override
    public void set_FortressPos(@Nullable BlockPos blockPos) {
        this.dataTracker.set(FORTRESS_POS, Optional.ofNullable(blockPos));
    }
}
