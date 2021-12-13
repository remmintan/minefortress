package org.minefortress.mixins.interaction;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import org.minefortress.FortressEntities;
import org.minefortress.entity.Colonist;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicLong;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class FortressServerInteractionManagerMixin {

    private static final int DEFAULT_COLONIST_COUNT = 5;

    @Shadow
    @Final
    protected ServerPlayerEntity player;
    @Shadow
    protected ServerWorld world;


    @Inject(method = "setGameMode", at = @At(value = "TAIL"))
    protected void setGameMode(GameMode gameMode, GameMode previousGameMode, CallbackInfo ci) {

        if(gameMode == ClassTinkerers.getEnum(GameMode.class, "FORTRESS")) {
            BlockPos blockPos = player.getBlockPos();
            AtomicLong spawned = new AtomicLong();
            world.iterateEntities().forEach(entity -> {
                if(entity instanceof Colonist && entity.isAlive()) {
                    spawned.getAndIncrement();
                }
            });
            int needToSpawn = (int)Math.max(DEFAULT_COLONIST_COUNT - spawned.get(), 0);

            EntityType<?> colonistType = EntityType.get("minefortress:colonist").orElseThrow();
            Iterable<BlockPos> spawnPlaces = BlockPos.iterateRandomly(world.random, needToSpawn, blockPos, 3);
            for(BlockPos spawnPlace : spawnPlaces) {
                int spawnY = world.getTopY(Heightmap.Type.WORLD_SURFACE, spawnPlace.getX(), spawnPlace.getZ());
                BlockPos spawnPos = new BlockPos(spawnPlace.getX(), spawnY, spawnPlace.getZ());
                colonistType.spawn(world, null, null, player, spawnPos, SpawnReason.MOB_SUMMONED, true, false);
            }

        }

    }
}
