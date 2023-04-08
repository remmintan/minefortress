package org.minefortress.fight.influence;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.network.s2c.S2CSyncInfluence;

import java.util.ArrayList;
import java.util.List;

public class ServerInfluenceManager {

    private final List<BlockPos> allInfluencePositions = new ArrayList<>();
    private final Synchronizer synchronizer = new Synchronizer();

    public void addInfluencePosition(BlockPos pos) {
        allInfluencePositions.add(pos);
        synchronizer.scheduleSync();
    }

    public void tick(ServerPlayerEntity player) {
        synchronizer.tick(player);
    }

    public void sync() {
        synchronizer.scheduleSync();
    }

    public void write(NbtCompound tag) {
        final var nbt = new NbtCompound();
        final var list = new NbtList();
        for (final var pos : allInfluencePositions) {
            final var posTag = new NbtCompound();
            posTag.putLong("pos", pos.asLong());
            list.add(posTag);
        }
        nbt.put("positions", list);

        tag.put("influenceManager", nbt);
    }

    public void read(NbtCompound tag) {
        if (!tag.contains("influenceManager")) {
            return;
        }
        NbtCompound nbt = tag.getCompound("influenceManager");

        allInfluencePositions.clear();
        final var list = nbt.getList("positions", NbtList.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            final var posTag = list.getCompound(i);
            final var pos = BlockPos.fromLong(posTag.getLong("pos"));
            allInfluencePositions.add(pos);
        }
        synchronizer.scheduleSync();
    }

    private class Synchronizer {

        private boolean syncScheduled = false;

        public void scheduleSync() {
            if (syncScheduled) {
                return;
            }
            syncScheduled = true;
        }

        public void tick(ServerPlayerEntity player) {
            if (!syncScheduled) {
                return;
            }
            final var s2CSyncInfluence = new S2CSyncInfluence(allInfluencePositions);
            FortressServerNetworkHelper.send(player, S2CSyncInfluence.CHANNEL,  s2CSyncInfluence);
            syncScheduled = false;
        }
    }

}
