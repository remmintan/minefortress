package org.minefortress.utils;

import com.google.common.collect.Streams;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.List;

public class AreasUtils {

    public static List<BlockPos> buildAnAreaOnSurfaceWithinBlocks(Iterable<BlockPos> blocks, World world) {
        return Streams
                .stream(blocks)
                .map(BlockPos::toImmutable)
                .flatMap(it -> {
                    final var topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, it.getX(), it.getZ());
                    return Streams.stream(BlockPos.iterate(it.withY(topY-3), it.withY(topY+3))).map(BlockPos::toImmutable);
                })
                .toList();
    }

}
