package org.minefortress.tasks

import com.mojang.datafixers.util.Pair
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.TaskType
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper
import net.remmintan.mods.minefortress.networking.s2c.ClientboundTaskExecutedPacket
import org.minefortress.tasks.block.info.ReplaceTaskBlockInfo
import java.util.*
import kotlin.math.max

class RoadsTask(override val positions: List<BlockPos>, private val item: Item) : ITask {
    private val taskParts: Queue<ITaskPart> = ArrayDeque()
    private val totalParts: Int
    private var finishedParts = 0

    private var canceled = false

    private var assignedWorkers = 0

    override val taskType = TaskType.REPLACE
    val requiredItems: List<ItemStack> = listOf(ItemStack(item, positions.size))

    init {
        this.totalParts = prepareParts()
    }

    private fun prepareParts(): Int {
        val partBlocks: MutableList<BlockPos> = ArrayList()
        var partCounter = 0
        for (block in positions) {
            partBlocks.add(block)
            if (partCounter++ > 9) {
                val taskPart = createTaskPart(partBlocks)
                taskParts.add(taskPart)
                partBlocks.clear()
                partCounter = 0
            }
        }

        if (partBlocks.isNotEmpty()) {
            val taskPart = createTaskPart(partBlocks)
            taskParts.add(taskPart)
        }

        return taskParts.size
    }

    private fun createTaskPart(partBlocks: List<BlockPos>): ITaskPart {
        val first = partBlocks[0]
        val last = partBlocks[partBlocks.size - 1]

        val partStartAndEnd = Pair.of(first, last)

        val blocks = partBlocks.map { ReplaceTaskBlockInfo(it, item) }
        return TaskPart(partStartAndEnd, blocks, this)
    }

    override fun hasAvailableParts(): Boolean {
        return !taskParts.isEmpty()
    }

    override fun getNextPart(colonist: IWorkerPawn): ITaskPart? {
        return taskParts.poll()
    }

    override fun returnPart(partStartAndEnd: Pair<BlockPos, BlockPos>) {
        val partStart = partStartAndEnd.first
        val i = positions.indexOf(partStart)
        if (i != -1) {
            val partBlocks = ArrayList<BlockPos>()
            for (j in i..<positions.size) {
                if (j - i > 10) break
                partBlocks.add(positions[j])
            }
            val taskPart = createTaskPart(partBlocks)
            taskParts.add(taskPart)
        }
    }

    override fun finishPart(part: ITaskPart, colonist: IWorkerPawn) {
        val world = colonist.serverWorld
        finishedParts++
        check(finishedParts <= totalParts) { "Finished more parts than total parts" }

        if (taskParts.isEmpty() && totalParts == finishedParts) {
            world.players.stream().findAny().ifPresent { player: ServerPlayerEntity? ->
                FortressServerNetworkHelper.send(
                    player,
                    FortressChannelNames.FINISH_TASK,
                    ClientboundTaskExecutedPacket(this.pos)
                )
            }
        }
    }

    override fun cancel() {
        canceled = true
    }

    override fun notCancelled(): Boolean {
        return !canceled
    }

    override fun toTaskInformationDto(): TaskInformationDto {
        return TaskInformationDto(pos, positions, taskType)
    }

    override fun isComplete(): Boolean {
        return finishedParts == totalParts
    }

    override fun canTakeMoreWorkers(): Boolean {
        return assignedWorkers < max((totalParts / 2).toDouble(), 1.0)
    }

    override fun addWorker() {
        assignedWorkers++
    }

    override fun removeWorker() {
        assignedWorkers++
    }
}
