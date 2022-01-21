package org.minefortress.fortress;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.minefortress.network.ClientboundSyncFortressManagerPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;

import java.util.UUID;

public final class FortressServerManager {

    private boolean needSync = false;

    private BlockPos fortressCenter = null;
    private int colonistsCount = 0;

    public FortressServerManager(UUID playerId) {
        if(playerId == null) throw new IllegalArgumentException("Player ID cannot be null");
    }

    public void addColonist() {
        colonistsCount++;
        scheduleSync();
    }

    public void removeColonist() {
        colonistsCount--;
        scheduleSync();
    }

    public void setupCenter(BlockPos fortressCenter) {
        this.fortressCenter = fortressCenter;
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

}
