package org.minefortress.utils;

import I;
import com.google.common.collect.Streams;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;

public class AreasUtils {

    public static List<BlockPos> buildAnAreaOnSurfaceWithinBlocks(Iterable<BlockPos> blocks, World world, Heightmap.Type heightmapType) {
        return Streams
                .stream(blocks)
                .map(BlockPos::toImmutable)
                .flatMap(it -> {
                    final var topY = world.getTopY(heightmapType, it.getX(), it.getZ());
                    return Streams.stream(BlockPos.iterate(it.withY(topY-3), it.withY(topY+3))).map(BlockPos::toImmutable);
                })
                .sorted(
                    Comparator.comparingInt(BlockPos::getY)
                        .reversed()
                        .thenComparing(BlockPos::getX)
                        .thenComparing(BlockPos::getZ)
                )
                .toList();
    }

}
