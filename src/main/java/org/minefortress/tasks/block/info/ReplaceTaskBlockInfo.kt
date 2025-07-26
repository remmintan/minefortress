package org.minefortress.tasks.block.info

import net.minecraft.block.BlockState
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.TaskType
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo
import org.minefortress.utils.BlockUtils

class ReplaceTaskBlockInfo(private val pos: BlockPos, private val placingItem: Item) : ITaskBlockInfo {

    val state: BlockState = BlockUtils.getBlockStateFromItem(placingItem)

    override fun getType(): TaskType = TaskType.REPLACE

    override fun getPlacingItem(): Item = placingItem

    override fun getPos(): BlockPos = pos

}