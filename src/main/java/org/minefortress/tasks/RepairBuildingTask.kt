package org.minefortress.tasks

import net.minecraft.block.BlockState
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.TaskType
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo
import java.util.*

class RepairBuildingTask(
    startingBlock: BlockPos,
    endingBlock: BlockPos,
    blocksToRepair: Map<BlockPos, BlockState>,
    val repairItems: List<ItemStack>
) :
    AbstractTask(TaskType.BUILD, startingBlock, endingBlock) {
    private val blocksToRepair: Map<BlockPos, BlockState> =
        Collections.unmodifiableMap(blocksToRepair)

    override val positions: List<BlockPos>
        get() = blocksToRepair.keys.stream().toList()

    override fun getNextPart(colonist: IWorkerPawn): ITaskPart {
        val part = parts.remove()
        val taskBlocks = BlockPos.stream(part.first, part.second)
            .map { obj: BlockPos -> obj.toImmutable() }
            .filter { key: BlockPos -> blocksToRepair.containsKey(key) }
            .map { it: BlockPos ->
                val state = blocksToRepair[it]
                val item = Item.BLOCK_ITEMS[state!!.block]
                BlockStateTaskBlockInfo(item, it, state)
            }
            .map { obj: BlockStateTaskBlockInfo? -> ITaskBlockInfo::class.java.cast(obj) }
            .toList()

        return TaskPart(part, taskBlocks, this)
    }

    override fun toTaskInformationDto(): TaskInformationDto {
        val taskInfoDto = TaskInformationDto(pos, positions, taskType)
        return taskInfoDto
    }
}
