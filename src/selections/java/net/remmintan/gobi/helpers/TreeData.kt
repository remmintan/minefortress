package net.remmintan.gobi.helpers

import net.minecraft.util.math.BlockPos

data class TreeData(val treeLogBlocks: Set<BlockPos>, val treeLeavesBlocks: Set<BlockPos>, val treeRootBlock: BlockPos)
