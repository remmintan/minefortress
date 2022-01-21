package org.minefortress.mixins.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class FortressServerPlayerEntityMixin extends PlayerEntity implements FortressServerPlayerEntity {

    private FortressServerManager fortressServerManager;

    public FortressServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method="<init>", at=@At("RETURN"))
    public void init(MinecraftServer server, ServerWorld world, GameProfile profile, CallbackInfo ci) {
        fortressServerManager = new FortressServerManager(this.getUuid());
    }

    @Inject(method="tick", at=@At("TAIL"))
    public void tick(CallbackInfo ci) {
        fortressServerManager.tick((ServerPlayerEntity)(Object)this);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        final NbtCompound fortressManagerTag = new NbtCompound();
        fortressServerManager.writeToNbt(fortressManagerTag);
        nbt.put("FortressManager", fortressManagerTag);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        final NbtCompound fortressManagerTag = nbt.getCompound("FortressManager");
        fortressServerManager.readFromNbt(fortressManagerTag);
    }

    @Override
    public FortressServerManager getFortressServerManager() {
        return fortressServerManager;
    }
}
