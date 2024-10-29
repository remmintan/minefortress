package org.minefortress.mixins.entity.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IServerBlueprintManager;
import net.remmintan.mods.minefortress.core.interfaces.entities.player.FortressServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.minefortress.MineFortressMod;
import org.minefortress.blueprints.manager.ServerBlueprintManager;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.utils.FortressSpawnLocating;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class FortressServerPlayerEntityMixin extends PlayerEntity implements FortressServerPlayerEntity {

    @Shadow @Final public MinecraftServer server;
    @Shadow public ServerPlayNetworkHandler networkHandler;
    private Vec3d persistedPos;
    private Vec3d persistedVelocity;
    private float persistedYaw;
    private float persistedPitch;

    private IServerBlueprintManager serverBlueprintManager;

    private boolean wasInBlueprintWorldWhenLoggedOut = false;

    public FortressServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method="<init>", at=@At("RETURN"))
    public void init(MinecraftServer server, ServerWorld world, GameProfile profile, SyncedClientOptions clientOptions, CallbackInfo ci) {
        serverBlueprintManager = new ServerBlueprintManager(server);
    }

    @Inject(method="tick", at=@At("TAIL"))
    public void tick(CallbackInfo ci) {
        serverBlueprintManager.tick((ServerPlayerEntity)(Object)this);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        final var blueprintManager = serverBlueprintManager.write();
        nbt.put("blueprintManager", blueprintManager);

        NbtCompound playerState = new NbtCompound();
        playerState.putBoolean("wasInBlueprintWorldWhenLoggedOut", wasInBlueprintWorldWhenLoggedOut);
        if(persistedPos != null) {
            playerState.putDouble("persistedPosX", persistedPos.x);
            playerState.putDouble("persistedPosY", persistedPos.y);
            playerState.putDouble("persistedPosZ", persistedPos.z);
        }
        nbt.put("playerState", playerState);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        final var blueprintManager = nbt.contains("blueprintManager") ? nbt.getCompound("blueprintManager") : new NbtCompound();
        serverBlueprintManager.read(blueprintManager);
        if(nbt.contains("playerState")) {
            NbtCompound playerState = nbt.getCompound("playerState");
            wasInBlueprintWorldWhenLoggedOut = playerState.getBoolean("wasInBlueprintWorldWhenLoggedOut");
            if(playerState.contains("persistedPosX")) {
                persistedPos = new Vec3d(
                        playerState.getDouble("persistedPosX"),
                        playerState.getDouble("persistedPosY"),
                        playerState.getDouble("persistedPosZ")
                );
            }
        }

    }

    @Override
    public IServerBlueprintManager get_ServerBlueprintManager() {
        return serverBlueprintManager;
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if(oldPlayer instanceof FortressServerPlayerEntity fortressServerPlayer) {
            this.serverBlueprintManager = fortressServerPlayer.get_ServerBlueprintManager();
        }
    }


    @Inject(method="getTeleportTarget", at=@At("HEAD"), cancellable = true)
    public void getTeleportTarget(ServerWorld destination, CallbackInfoReturnable<TeleportTarget> cir) {
        if(destination.getRegistryKey() == BlueprintsWorld.BLUEPRINTS_WORLD_REGISTRY_KEY) {
            this.persistedPos = this.getPos();
            this.persistedVelocity = this.getVelocity();
            this.persistedPitch = this.getPitch();
            this.persistedYaw = this.getYaw();

            final Vec3d position = new Vec3d(-1, 17, -1);
            final Vec3d velocity = new Vec3d(0, 0, 0);
            final TeleportTarget teleportTarget = new TeleportTarget(position, velocity, -45, 0);
            cir.setReturnValue(teleportTarget);
        }

        if(this.getWorld().getRegistryKey() == BlueprintsWorld.BLUEPRINTS_WORLD_REGISTRY_KEY && destination.getRegistryKey() == World.OVERWORLD) {
            final Vec3d position = this.persistedPos;
            final Vec3d velocity = this.persistedVelocity;
            final TeleportTarget teleportTarget = new TeleportTarget(position, velocity, this.persistedYaw, this.persistedPitch);
            cir.setReturnValue(teleportTarget);
        }
    }

    @Redirect(method = "moveToSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/SpawnLocating;findOverworldSpawn(Lnet/minecraft/server/world/ServerWorld;II)Lnet/minecraft/util/math/BlockPos;"))
    public BlockPos moveToSpawnFindOverworldSpawn(ServerWorld world, int x, int z) {
        final BlockPos actualSpawn = FortressSpawnLocating.findOverworldSpawn(world, x, z);
        if(actualSpawn != null && this.server.getDefaultGameMode() == MineFortressMod.FORTRESS){
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

}
