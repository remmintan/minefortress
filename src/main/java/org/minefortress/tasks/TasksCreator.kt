package org.minefortress.tasks

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.remmintan.gobi.helpers.findTree
import net.remmintan.mods.minefortress.core.TaskType
import net.remmintan.mods.minefortress.core.interfaces.selections.ServerSelectionType
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksCreator
import java.util.*

class TasksCreator(private val serverWorld: ServerWorld) : ITasksCreator {
    override fun createCutTreesTask(uuid: UUID, treeRoots: List<BlockPos>): ITask {
        val visitedBlocks = mutableSetOf<BlockPos>()
        val trees = treeRoots.associateWith { findTree(it, serverWorld, visitedBlocks)!! }

        return CutTreesTask(uuid, trees)
    }

    override fun createRoadsTask(uuid: UUID, blocks: List<BlockPos>, itemInHand: Item): ITask {
        return RoadsTask(uuid, blocks, itemInHand)
    }

    override fun createSelectionTask(
        id: UUID,
        taskType: TaskType,
        start: BlockPos,
        end: BlockPos,
        selectionType: ServerSelectionType,
        hitResult: HitResult,
        positions: List<BlockPos>,
        player: ServerPlayerEntity
    ): ITask {
        val simpleSelectionTask = SimpleSelectionTask(id, taskType, start, end, hitResult, selectionType, positions)
        if (simpleSelectionTask.getTaskType() == TaskType.BUILD) {
            val itemInHand = player.getStackInHand(Hand.MAIN_HAND)
            if (itemInHand !== ItemStack.EMPTY) {
                simpleSelectionTask.placingItem = itemInHand.item
            } else {
                throw IllegalStateException()
            }
        }
        return simpleSelectionTask
    }
}
