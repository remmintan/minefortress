package org.minefortress.mixins.entity.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.blueprints.BlueprintsDimensionKt;
import net.remmintan.mods.minefortress.core.FortressGamemodeUtilsKt;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IServerBlueprintManager;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.BlueprintsDimensionUtilsKt;
import net.remmintan.mods.minefortress.core.interfaces.entities.player.FortressServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IPlayerManagersProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.blueprints.manager.ServerBlueprintManager;
import org.minefortress.utils.FortressSpawnLocating;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class FortressServerPlayerEntityMixin extends PlayerEntity implements FortressServerPlayerEntity, IPlayerManagersProvider {

    @Shadow @Final public MinecraftServer server;
    @Shadow public ServerPlayNetworkHandler networkHandler;

    @Unique
    private final IServerBlueprintManager blueprintManager = new ServerBlueprintManager();
    @Unique
    private Vec3d persistedPos;
    @Unique
    private Vec3d persistedVelocity;
    @Unique
    private float persistedYaw;
    @Unique
    private float persistedPitch;
    @Unique
    private boolean wasInBlueprintWorldWhenLoggedOut = false;

    @Shadow
    public abstract ServerWorld getServerWorld();

    public FortressServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound playerState = new NbtCompound();
        playerState.putBoolean("wasInBlueprintWorldWhenLoggedOut", wasInBlueprintWorldWhenLoggedOut);
        if(persistedPos != null) {
            playerState.putDouble("persistedPosX", persistedPos.x);
            playerState.putDouble("persistedPosY", persistedPos.y);
            playerState.putDouble("persistedPosZ", persistedPos.z);
        }

        blueprintManager.write(playerState);

        nbt.put("fortressPlayerState", playerState);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("fortressPlayerState")) {
            NbtCompound playerState = nbt.getCompound("fortressPlayerState");
            wasInBlueprintWorldWhenLoggedOut = playerState.getBoolean("wasInBlueprintWorldWhenLoggedOut");
            if (playerState.contains("persistedPosX")) {
                persistedPos = new Vec3d(
                        playerState.getDouble("persistedPosX"),
                        playerState.getDouble("persistedPosY"),
                        playerState.getDouble("persistedPosZ")
                );
            }
            blueprintManager.read(playerState);
        }
    }


    @Inject(method="getTeleportTarget", at=@At("HEAD"), cancellable = true)
    public void getTeleportTarget(ServerWorld destination, CallbackInfoReturnable<TeleportTarget> cir) {
        if (destination.getRegistryKey() == BlueprintsDimensionUtilsKt.getBLUEPRINT_DIMENSION_KEY()) {
            this.persistedPos = this.getPos();
            this.persistedVelocity = this.getVelocity();
            this.persistedPitch = this.getPitch();
            this.persistedYaw = this.getYaw();

            final var cell = BlueprintsDimensionKt.getPersonalBlueprintCell(this);

            final var configBlockPos = cell.getConfigBlock();
            final Vec3d position = new Vec3d(configBlockPos.getX(), configBlockPos.getY() + 1, configBlockPos.getZ());
            final Vec3d velocity = new Vec3d(0, 0, 0);
            final TeleportTarget teleportTarget = new TeleportTarget(position, velocity, -45, 0);
            cir.setReturnValue(teleportTarget);
        }

        if (this.getWorld().getRegistryKey() == BlueprintsDimensionUtilsKt.getBLUEPRINT_DIMENSION_KEY() && destination.getRegistryKey() == World.OVERWORLD) {
            final Vec3d position = this.persistedPos;
            final Vec3d velocity = this.persistedVelocity;
            final TeleportTarget teleportTarget = new TeleportTarget(position, velocity, this.persistedYaw, this.persistedPitch);
            cir.setReturnValue(teleportTarget);
        }
    }

    @Redirect(method = "moveToSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/SpawnLocating;findOverworldSpawn(Lnet/minecraft/server/world/ServerWorld;II)Lnet/minecraft/util/math/BlockPos;"))
    public BlockPos moveToSpawnFindOverworldSpawn(ServerWorld world, int x, int z) {
        final BlockPos actualSpawn = FortressSpawnLocating.findOverworldSpawn(world, x, z);
        if (actualSpawn != null && this.server.getDefaultGameMode() == FortressGamemodeUtilsKt.getFORTRESS()) {
            return actualSpawn.up(20);
        } else {
            return actualSpawn;
        }
    }

    @Inject(method = "moveToSpawn", at = @At("RETURN"))
    public void moveToSpawnAfter(ServerWorld world, CallbackInfo ci) {
        this.setYaw(45f);
        this.setPitch(60f);
        final var blockPos = this.getBlockPos();
        if(this.networkHandler!=null)
            this.teleport(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        this.blueprintManager.tick(this.server, this.getServerWorld(), (ServerPlayerEntity) (Object) this);
    }

    @Override
    public boolean was_InBlueprintWorldWhenLoggedOut() {
        return wasInBlueprintWorldWhenLoggedOut;
    }

    @Override
    public void set_WasInBlueprintWorldWhenLoggedOut(boolean wasInBlueprintWorldWhenLoggedOut) {
        this.wasInBlueprintWorldWhenLoggedOut = wasInBlueprintWorldWhenLoggedOut;
    }

    @Override
    @Nullable
    public Vec3d get_PersistedPos() {
        return persistedPos;
    }

    @Override
    public @NotNull IServerBlueprintManager get_BlueprintManager() {
        return blueprintManager;
    }
}
