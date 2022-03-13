package org.minefortress.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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

}
