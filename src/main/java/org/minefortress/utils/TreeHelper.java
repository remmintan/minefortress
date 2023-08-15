package org.minefortress.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class TreeHelper {

    public static Optional<TreeInfo> checkIfTree(BlockPos treeRoot, World world) {
        int logCount = 0;
        int leavesCount = 0;

        BlockPos highestLeaf = BlockPos.ORIGIN;

        for (int i = 0; i < 32; i++) {
            BlockPos layerStart = new BlockPos(treeRoot.getX() - 2, treeRoot.getY() + i, treeRoot.getZ() - 2);
            BlockPos layerEnd = new BlockPos(treeRoot.getX() + 2, treeRoot.getY() + i, treeRoot.getZ() + 2);

            boolean layerIsEmpty = true;

            for(BlockPos pos : BlockPos.iterate(layerStart, layerEnd)) {
                final BlockState blockState = world.getBlockState(pos);
                if(isLog(blockState)){
                    logCount++;
                    layerIsEmpty = false;
                }
                if(isLeaves(blockState)){
                    leavesCount++;
                    layerIsEmpty = false;
                    if(pos.getY() > highestLeaf.getY()) highestLeaf = pos.toImmutable();
                }
            }

            if(layerIsEmpty) {
                break;
            }
        }

        if(logCount == 0 || leavesCount < 9) return Optional.empty();

        return Optional.of(new TreeInfo(logCount, highestLeaf));
    }

    public static boolean isLog(BlockState blockState) {
        return blockState.isIn(BlockTags.LOGS);
    }

    public static boolean isLeaves(BlockState blockState) {
        return blockState.isIn(BlockTags.LEAVES)  || blockState.getBlock() instanceof LeavesBlock;
    }

    public static Optional<TreeBlocks> getTreeBlocks(BlockPos root, World world) {
        final Optional<TreeInfo> treeInfoOpt = checkIfTree(root, world);
        if(treeInfoOpt.isPresent()) {
            BlockState rootBlockState = world.getBlockState(root);
            final ArrayList<BlockPos> treeBlocks = new ArrayList<>();
            final ArrayList<BlockPos> leavesBlocks = new ArrayList<>();
            updateTreeDataForOneTree(world, treeBlocks, leavesBlocks, root, rootBlockState, root);
            return Optional.of(new TreeBlocks(treeBlocks, leavesBlocks));
        } else {
            return Optional.empty();
        }
    }

    private static void updateTreeDataForOneTree(World world, List<BlockPos> treeBlocks, List<BlockPos> leavesBlocks, BlockPos cursor, BlockState rootBlockState, BlockPos root) {
        if(!isLog(rootBlockState)) return;
        BlockPos areaStart = new BlockPos(cursor.getX() - 1, cursor.getY(), cursor.getZ() - 1);
        BlockPos areaEnd = new BlockPos(cursor.getX() + 1, cursor.getY() + 1, cursor.getZ() + 1);
        List<BlockPos> neighbors = new ArrayList<>();
        for(BlockPos pos: BlockPos.iterate(areaStart, areaEnd)) {
            pos = pos.toImmutable();
            if(treeBlocks.contains(pos) || leavesBlocks.contains(pos)) continue;
            final BlockState blockState = world.getBlockState(pos);
            if(rootBlockState.equals(blockState)) {
                treeBlocks.add(pos);
                neighbors.add(pos);
            } else if(isLeaves(blockState)) {
                final double distanceToRoot = Math.sqrt(Math.pow(pos.getX() - root.getX(), 2) + Math.pow(pos.getZ() - root.getZ(), 2));
                if(distanceToRoot <= 3) {
                    leavesBlocks.add(pos);
                    neighbors.add(pos);
                }
            }
        }

        for (BlockPos neighbor : neighbors) {
            updateTreeDataForOneTree(world, treeBlocks, leavesBlocks, neighbor, rootBlockState, root);
        }
    }

    static class TreeInfo {
        private final int logsCount;
        private final BlockPos highestLeaf;

        public TreeInfo(int logsCount, BlockPos highestLeaf) {
            this.logsCount = logsCount;
            this.highestLeaf = highestLeaf;
        }


        public int getLogsCount() {
            return logsCount;
        }

        public BlockPos getHighestLeaf() {
            return highestLeaf;
        }
    }

}
