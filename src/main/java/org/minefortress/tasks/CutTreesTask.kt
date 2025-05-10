package org.minefortress.tasks

import com.mojang.datafixers.util.Pair
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import net.remmintan.gobi.helpers.TreeData
import net.remmintan.gobi.helpers.TreeRemover
import net.remmintan.mods.minefortress.core.TaskType
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart
import net.remmintan.mods.minefortress.core.utils.ServerModUtils
import net.remmintan.mods.minefortress.core.utils.getFortressOwner
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper
import net.remmintan.mods.minefortress.networking.s2c.ClientboundTaskExecutedPacket
import org.minefortress.tasks.block.info.DigTaskBlockInfo
import java.util.*

class CutTreesTask(private val uuid: UUID, private val trees: Map<BlockPos, TreeData>) : ITask {
    private val treeRoots: Queue<BlockPos> = ArrayDeque(trees.keys)
    private val totalTreesCount = treeRoots.size

    private var removedTrees = 0
    private var canceled = false

    override fun getId(): UUID {
        return uuid
    }

    override fun getTaskType(): TaskType {
        return TaskType.REMOVE
    }

    override fun hasAvailableParts(): Boolean {
        return !treeRoots.isEmpty()
    }

    override fun getNextPart(colonist: IWorkerPawn): ITaskPart? {
        return treeRoots
            .poll()
            ?.let { TaskPart(Pair.of(it, it), listOf(DigTaskBlockInfo(it)), this) }
    }

    override fun returnPart(partStartAndEnd: Pair<BlockPos, BlockPos>) {
        val root = partStartAndEnd.first
        treeRoots.add(root)
    }

    override fun cancel() {
        canceled = true
    }

    override fun notCancelled(): Boolean {
        return canceled
    }

    override fun finishPart(part: ITaskPart, pawn: IWorkerPawn) {
        val root = part.startAndEnd.first
        val tree = trees[root] ?: return
        val managersProvider = ServerModUtils.getManagersProvider(pawn).orElseThrow()
        TreeRemover(pawn.serverWorld, managersProvider.resourceManager, pawn as LivingEntity).removeTheTree(tree)

        removedTrees++
        check(removedTrees <= totalTreesCount) { "Removed more roots than total roots" }

        if (treeRoots.isEmpty() && removedTrees == totalTreesCount) {
            (pawn as IWorkerPawn).server.getFortressOwner(pawn.fortressPos!!)?.let {
                FortressServerNetworkHelper.send(
                    it,
                    FortressChannelNames.FINISH_TASK,
                    ClientboundTaskExecutedPacket(this.getId())
                )
            }
        }
    }

    override fun isComplete(): Boolean {
        return removedTrees == totalTreesCount
    }

    override fun toTaskInformationDto(): List<TaskInformationDto> {
        val positions = trees.values.flatMap { listOf(it.treeLogBlocks, it.treeLeavesBlocks).flatten() }
        return listOf(TaskInformationDto(getId(), positions, TaskType.REMOVE))
    }
}
