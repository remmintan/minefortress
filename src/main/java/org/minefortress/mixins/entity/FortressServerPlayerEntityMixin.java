package org.minefortress.mixins.entity;

import com.chocohead.mm.api.ClassTinkerers;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.minefortress.MineFortressMod;
import org.minefortress.blueprints.manager.ServerBlueprintManager;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.ClientboundFollowColonistPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.tasks.TaskManager;
import org.minefortress.utils.FortressSpawnLocating;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public abstract class FortressServerPlayerEntityMixin extends PlayerEntity implements FortressServerPlayerEntity {

    private UUID fortressUUID = UUID.randomUUID();

    private Vec3d persistedPos;
    private Vec3d persistedVelocity;
    private float persistedYaw;
    private float persistedPitch;

    @Shadow @Final public ServerPlayerInteractionManager interactionManager;
    @Shadow @Final public MinecraftServer server;
    private FortressServerManager fortressServerManager;
    private ServerBlueprintManager serverBlueprintManager;
    private final TaskManager taskManager = new TaskManager();

    public FortressServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method="<init>", at=@At("RETURN"))
    public void init(MinecraftServer server, ServerWorld world, GameProfile profile, CallbackInfo ci) {
        fortressServerManager = new FortressServerManager();
        serverBlueprintManager = new ServerBlueprintManager(server);
    }

    @Inject(method="tick", at=@At("TAIL"))
    public void tick(CallbackInfo ci) {
        fortressServerManager.tick((ServerPlayerEntity)(Object)this);
        serverBlueprintManager.tick((ServerPlayerEntity)(Object)this);
        taskManager.tick(fortressServerManager, (ServerWorld) world);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putUuid("fortressUuid", fortressUUID);
        final NbtCompound fortressManagerTag = new NbtCompound();
        fortressServerManager.writeToNbt(fortressManagerTag);
        nbt.put("FortressManager", fortressManagerTag);

        serverBlueprintManager.writeToNbt(nbt);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if(nbt.contains("fortressUuid")) {
            fortressUUID = nbt.getUuid("fortressUuid");
        }
        if(nbt.contains("FortressManager")) {
            final NbtCompound fortressManagerTag = nbt.getCompound("FortressManager");
            fortressServerManager.readFromNbt(fortressManagerTag);
        }
        serverBlueprintManager.readFromNbt(nbt);
    }

    @Override
    public FortressServerManager getFortressServerManager() {
        return fortressServerManager;
    }

    @Override
    public ServerBlueprintManager getServerBlueprintManager() {
        return serverBlueprintManager;
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void attack(Entity target, CallbackInfo ci) {
        final GameMode gameMode = this.interactionManager.getGameMode();
        if(gameMode != MineFortressMod.FORTRESS) return;

        if(target instanceof Colonist colonist) {
            final int id = colonist.getId();
            final ClientboundFollowColonistPacket packet = new ClientboundFollowColonistPacket(id);
            FortressServerNetworkHelper.send((ServerPlayerEntity) (Object)this, FortressChannelNames.FORTRESS_FOLLOW_COLONIST, packet);
        }

        ci.cancel();
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if(oldPlayer instanceof FortressServerPlayerEntity fortressServerPlayer) {
            this.fortressServerManager = fortressServerPlayer.getFortressServerManager();
            this.serverBlueprintManager = fortressServerPlayer.getServerBlueprintManager();
        }
    }

    @Override
    public UUID getFortressUuid() {
        return fortressUUID;
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

        if(this.world.getRegistryKey() == BlueprintsWorld.BLUEPRINTS_WORLD_REGISTRY_KEY && destination.getRegistryKey() == World.OVERWORLD) {
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

    @Override
    public TaskManager getTaskManager() {
        return this.taskManager;
    }

    @Override
    public boolean isFortressSurvival() {
        return interactionManager != null && interactionManager.getGameMode() == MineFortressMod.FORTRESS &&
                fortressServerManager.isSurvival();
    }
}
