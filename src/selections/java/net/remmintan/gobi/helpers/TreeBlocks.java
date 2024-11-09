package net.remmintan.gobi.helpers;

import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public record TreeBlocks(List<BlockPos> treeBlocks, List<BlockPos> leavesBlocks) {

    public TreeBlocks(List<BlockPos> treeBlocks, List<BlockPos> leavesBlocks) {
        this.treeBlocks = Collections.unmodifiableList(treeBlocks);
        this.leavesBlocks = Collections.unmodifiableList(leavesBlocks);
    }
}
