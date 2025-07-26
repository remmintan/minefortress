package org.minefortress.tasks

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.remmintan.gobi.helpers.TreeFinder
import net.remmintan.mods.minefortress.core.TaskType
import net.remmintan.mods.minefortress.core.interfaces.selections.ServerSelectionType
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksCreator

class TasksCreator(private val serverWorld: ServerWorld) : ITasksCreator {
    override fun createCutTreesTask(treeRoots: List<BlockPos>): ITask {
        val treeFinder = TreeFinder(serverWorld)
        val trees = treeRoots.associateWith { treeFinder.findTree(it)!! }

        return CutTreesTask(trees)
    }

    override fun createRoadsTask(
        blocks: List<BlockPos>,
        itemInHand: Item?
    ): ITask {
        return RoadsTask(blocks, itemInHand)
    }

    override fun createSelectionTask(
        taskType: TaskType,
        start: BlockPos,
        end: BlockPos,
        selectionType: ServerSelectionType,
        hitResult: HitResult,
        positions: List<BlockPos>,
        player: ServerPlayerEntity
    ): ITask {
        val placingItem = if (taskType == TaskType.BUILD) {
            val itemInHand = player.getStackInHand(Hand.MAIN_HAND)
            check(itemInHand !== ItemStack.EMPTY)
            itemInHand.item
        } else {
            null
        }
        return SimpleSelectionTask(taskType, start, end, hitResult, selectionType, positions, placingItem)
    }
}
