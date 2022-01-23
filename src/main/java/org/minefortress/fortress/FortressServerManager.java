package org.minefortress.fortress;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.minefortress.network.ClientboundSyncFortressManagerPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;

import java.util.UUID;

public final class FortressServerManager extends AbstractFortressManager {

    private static final int DEFAULT_COLONIST_COUNT = 5;

    private boolean needSync = true;

    private BlockPos fortressCenter = null;
    private int colonistsCount = 0;

    public void addColonist() {
        colonistsCount++;
        scheduleSync();
    }

    public void removeColonist() {
        colonistsCount--;
        scheduleSync();
    }

    public void setupCenter(BlockPos fortressCenter, World world, ServerPlayerEntity player) {
        if(fortressCenter == null) throw new IllegalArgumentException("Center cannot be null");
        this.fortressCenter = fortressCenter;

        if(!(world instanceof ServerWorld serverWorld))
            throw new IllegalArgumentException("World must be a server world");

        world.setBlockState(fortressCenter, getStateForCampCenter(), 3);
        world.emitGameEvent(player, GameEvent.BLOCK_PLACE, fortressCenter);

        final NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putUuid("playerId", player.getUuid());
        nbtCompound.putInt("centerX", fortressCenter.getX());
        nbtCompound.putInt("centerY", fortressCenter.getY());
        nbtCompound.putInt("centerZ", fortressCenter.getZ());

        EntityType<?> colonistType = EntityType.get("minefortress:colonist").orElseThrow();
        Iterable<BlockPos> spawnPlaces = BlockPos.iterateRandomly(world.random, DEFAULT_COLONIST_COUNT, fortressCenter, 3);
        for(BlockPos spawnPlace : spawnPlaces) {
            int spawnY = world.getTopY(Heightmap.Type.WORLD_SURFACE, spawnPlace.getX(), spawnPlace.getZ());
            BlockPos spawnPos = new BlockPos(spawnPlace.getX(), spawnY, spawnPlace.getZ());
            colonistType.spawn(serverWorld, nbtCompound, null, player, spawnPos, SpawnReason.MOB_SUMMONED, true, false);
        }

        this.scheduleSync();
    }

    public void tick(ServerPlayerEntity player) {
        if(!needSync) return;
        final ClientboundSyncFortressManagerPacket packet = new ClientboundSyncFortressManagerPacket(colonistsCount, fortressCenter);
        FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_MANAGER_SYNC, packet);
        needSync = false;
    }

    private void scheduleSync() {
        needSync = true;
    }

    public void writeToNbt(NbtCompound tag) {
        tag.putInt("colonistsCount", colonistsCount);
        if(fortressCenter != null) {
            tag.putInt("centerX", fortressCenter.getX());
            tag.putInt("centerY", fortressCenter.getY());
            tag.putInt("centerZ", fortressCenter.getZ());
        }
    }

    public void readFromNbt(NbtCompound tag) {
        colonistsCount = tag.getInt("colonistsCount");
        final int centerX = tag.getInt("centerX");
        final int centerY = tag.getInt("centerY");
        final int centerZ = tag.getInt("centerZ");
        if(centerX != 0 || centerY != 0 || centerZ != 0) {
            fortressCenter = new BlockPos(centerX, centerY, centerZ);
        }
        this.scheduleSync();
    }

    public BlockPos getFortressCenter() {
        return fortressCenter;
    }
}
