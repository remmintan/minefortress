package net.remmintan.gobi.helpers;

import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class TreeBlocks {

    private final List<BlockPos> treeBlocks;
    private final List<BlockPos> leavesBlocks;

    public TreeBlocks(List<BlockPos> treeBlocks, List<BlockPos> leavesBlocks) {
        this.treeBlocks = Collections.unmodifiableList(treeBlocks);
        this.leavesBlocks = Collections.unmodifiableList(leavesBlocks);
    }

    public List<BlockPos> getTreeBlocks() {
        return treeBlocks;
    }

    public List<BlockPos> getLeavesBlocks() {
        return leavesBlocks;
    }
}
