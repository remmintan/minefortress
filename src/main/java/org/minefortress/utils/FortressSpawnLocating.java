package org.minefortress.utils;

import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class FortressSpawnLocating extends SpawnLocating {

    public static BlockPos findOverworldSpawn(ServerWorld world, int x, int z) {
        return SpawnLocating.findOverworldSpawn(world, x, z);
    }

}
