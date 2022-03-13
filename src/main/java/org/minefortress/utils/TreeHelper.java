package org.minefortress.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreeHelper {

    public static int checkIfTree(BlockPos treeRoot, World world) {
        int logCount = 0;
        int leavesCount = 0;
        for (int i = 0; i < 29; i++) {
            BlockPos layerStart = new BlockPos(treeRoot.getX() - 2, treeRoot.getY() + i, treeRoot.getZ() - 2);
            BlockPos layerEnd = new BlockPos(treeRoot.getX() + 2, treeRoot.getY() + i, treeRoot.getZ() + 2);

            boolean layerIsEmpty = true;

            for(BlockPos pos : BlockPos.iterate(layerStart, layerEnd)) {
                final BlockState blockState = world.getBlockState(pos);
                final Block block = blockState.getBlock();
                if(isLog(block)){
                    logCount++;
                    layerIsEmpty = false;
                }
                if(isLeaves(block)){
                    leavesCount++;
                    layerIsEmpty = false;
                }
            }

            if(layerIsEmpty) {
                break;
            }
        }

        if(logCount == 0 || leavesCount < 9) return 0;

        return logCount;
    }

    public static boolean isLog(Block block) {
        return BlockTags.LOGS.contains(block);
    }

    public static boolean isLeaves(Block block) {
        return BlockTags.LEAVES.contains(block);
    }

    public static List<BlockPos> getTreeBlocks(BlockPos root, World world) {
        final int logsCount = checkIfTree(root, world);
        if(logsCount > 0) {
            Block rootBlock = world.getBlockState(root).getBlock();
            final ArrayList<BlockPos> treeBlocks = new ArrayList<>();
            updateTreeDataForOneTree(world, treeBlocks, root, rootBlock);
            return treeBlocks;
        } else {
            return Collections.emptyList();
        }
    }

    private static void updateTreeDataForOneTree(World world, List<BlockPos> treeBlocks, BlockPos root, Block rootBlock) {
        if(!isLog(rootBlock)) return;
        BlockPos areaStart = new BlockPos(root.getX() - 1, root.getY(), root.getZ() - 1);
        BlockPos areaEnd = new BlockPos(root.getX() + 1, root.getY() + 1, root.getZ() + 1);
        List<BlockPos> neighbors = new ArrayList<>();
        for(BlockPos pos: BlockPos.iterate(areaStart, areaEnd)) {
            pos = pos.toImmutable();
            if(treeBlocks.contains(pos)) continue;
            final BlockState blockState = world.getBlockState(pos);
            final Block block = blockState.getBlock();
            if(rootBlock.equals(block)) {
                treeBlocks.add(pos);
                neighbors.add(pos);
            }
        }

        for (BlockPos neighbor : neighbors) {
            updateTreeDataForOneTree(world, treeBlocks, neighbor, rootBlock);
        }
    }

}
