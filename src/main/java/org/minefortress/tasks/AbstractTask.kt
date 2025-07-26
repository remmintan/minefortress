package org.minefortress.tasks

import com.mojang.datafixers.util.Pair
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.remmintan.mods.minefortress.core.TaskType
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskWithPreparation
import net.remmintan.mods.minefortress.core.utils.PathUtils
import net.remmintan.mods.minefortress.core.utils.getFortressOwner
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper
import net.remmintan.mods.minefortress.networking.s2c.ClientboundTaskExecutedPacket
import java.util.*

private const val PART_SIZE = 3

abstract class AbstractTask protected constructor(
    override val taskType: TaskType,
    @JvmField protected var startingBlock: BlockPos,
    @JvmField protected var endingBlock: BlockPos
) : ITask, ITaskWithPreparation {
    @JvmField
    protected val parts: Queue<Pair<BlockPos, BlockPos>> = ArrayDeque()

    @JvmField
    protected var totalParts: Int = 0
    private var completedParts: Int = 0

    private var assignedWorkers = 0

    private var canceled: Boolean = false

    override val positions: List<BlockPos> = BlockPos.iterate(startingBlock, endingBlock).map { it.toImmutable() }


    override fun hasAvailableParts(): Boolean {
        return !parts.isEmpty()
    }

    override fun returnPart(partStartAndEnd: Pair<BlockPos, BlockPos>) {
        parts.add(partStartAndEnd)
    }

    override fun prepareTask() {
        val cursor = startingBlock.mutableCopy()
        val direction = PathUtils.getDirection(startingBlock, endingBlock)
        do {
            val start = cursor.toImmutable()
            val end = createPartEnd(start, direction)
            parts.add(Pair.of(start, end))

            if (end.x * direction.x >= endingBlock.x * direction.x) {
                if (end.z * direction.z >= endingBlock.z * direction.z) {
                    break
                } else {
                    cursor.setX(startingBlock.x)
                    cursor.move(0, 0, PART_SIZE * direction.z)
                }
            } else {
                cursor.move(direction.x * PART_SIZE, 0, 0)
            }
        } while (true)

        this.totalParts = parts.size
    }

    override fun finishPart(part: ITaskPart, colonist: IWorkerPawn) {
        completedParts++
        check(completedParts <= totalParts) { "Completed parts cannot be greater than total parts" }

        if (parts.isEmpty() && totalParts <= completedParts) {
            val owner = colonist.server.getFortressOwner(colonist.fortressPos!!)
            if (owner != null) {
                this.sendFinishTaskNotificationToPlayer(owner)
            }
        }
    }

    override fun isComplete(): Boolean {
        return totalParts <= completedParts
    }

    override fun cancel() {
        canceled = true
    }

    override fun notCancelled(): Boolean {
        return !canceled
    }

    override fun canTakeMoreWorkers(): Boolean {
        return assignedWorkers < parts.size
    }

    override fun removeWorker() {
        assignedWorkers--
    }

    override fun addWorker() {
        assignedWorkers++
    }

    override fun toTaskInformationDto(): TaskInformationDto {
        return TaskInformationDto(pos, positions, taskType)
    }

    protected fun sendFinishTaskNotificationToPlayer(player: ServerPlayerEntity?) {
        FortressServerNetworkHelper.send(
            player,
            FortressChannelNames.FINISH_TASK,
            ClientboundTaskExecutedPacket(pos)
        )
    }

    private fun createPartEnd(start: BlockPos, direction: Vec3i): BlockPos {
        val cursor = start.mutableCopy()
        cursor.setY(endingBlock.y)
        cursor.move((PART_SIZE - 1) * direction.x, 0, (PART_SIZE - 1) * direction.z)
        if (cursor.x * direction.x > endingBlock.x * direction.x) {
            cursor.setX(endingBlock.x)
        }
        if (cursor.z * direction.z > endingBlock.z * direction.z) {
            cursor.setZ(endingBlock.z)
        }
        return cursor.toImmutable()
    }
}
