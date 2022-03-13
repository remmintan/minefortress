package org.minefortress.selections;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RoadsSelection extends WallsSelection{

    @Override
    public boolean selectBlock(ClientWorld level, Item mainHandItem, BlockPos pickedBlock, int upDelta, ClickType click, ClientPlayNetworkHandler connection, HitResult hitResult) {
        if(click == ClickType.REMOVE) return true;

        if(corners.isEmpty()) {
            corners.add(pickedBlock.toImmutable());
            return false;
        } else {

            return true;
        }
    }

    protected List<BlockPos> getSelection(int upDelta, ArrayList<Pair<BlockPos, BlockPos>> cornerPairs) {
        final ClientWorld world = MinecraftClient.getInstance().world;
        if(Objects.isNull(world)) {
            return Collections.emptyList();
        }

        final BlockPos firstCorner = cornerPairs.isEmpty() ? null : cornerPairs.get(0).getFirst();

        return cornerPairs
                .stream()
                .map(p -> {
                    final BlockPos start = p.getFirst();
                    final BlockPos end = p.getSecond();

                    final boolean isFirstCorner = Objects.equals(start, firstCorner);

                    final BlockPos flatEnd = new BlockPos(end.getX(), start.getY() + upDelta, end.getZ());

                    final BlockPos direction = flatEnd.subtract(start);
                    final int expandAmount = 1 + upDelta;
                    if(direction.getZ() == 0) {
                        final int sign = isFirstCorner ? 0 : direction.getX() / Math.abs(direction.getX());
                        final BlockPos expandedStart = new BlockPos(start.getX() - sign * expandAmount, start.getY(), start.getZ() + expandAmount);
                        final BlockPos expandedEnd = new BlockPos(flatEnd.getX(), flatEnd.getY(), flatEnd.getZ() - expandAmount);

                        return BlockPos.iterate(expandedStart, expandedEnd);
                    }

                    if(direction.getX() == 0) {
                        final int sign = isFirstCorner ? 0 : direction.getZ() / Math.abs(direction.getZ());
                        final BlockPos expandedStart = new BlockPos(start.getX() + expandAmount, start.getY(), start.getZ() - sign * expandAmount);
                        final BlockPos expandedEnd = new BlockPos(flatEnd.getX() - expandAmount, flatEnd.getY(), flatEnd.getZ());

                        return BlockPos.iterate(expandedStart, expandedEnd);
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(WallsSelection::iterableToList)
                .map(pos -> {
                    BlockState blockState = world.getBlockState(pos);
                    while (isAir(blockState)) {
                        pos = pos.down();
                        blockState = world.getBlockState(pos);
                    }
                    while (!isAir(blockState)) {
                        pos = pos.up();
                        blockState = world.getBlockState(pos);
                    }
                    return pos.down();
                })
                .toList();
    }

    private boolean isAir(BlockState blockState) {
        final Block block = blockState.getBlock();
        return blockState.isAir() || BlockTags.FLOWERS.contains(block) || blockState.getMaterial().isReplaceable();
    }
}
