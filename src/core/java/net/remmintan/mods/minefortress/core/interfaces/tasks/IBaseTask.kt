package net.remmintan.mods.minefortress.core.interfaces.tasks

import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto

interface IBaseTask {

    val pos: BlockPos
        get() {
            require(positions.isNotEmpty())
            val box = BlockBox.encompassPositions(positions).get()
            val center = box.center
            return BlockPos(center.x, box.maxY + 1, center.z)
        }
    fun toTaskInformationDto(): TaskInformationDto
    val positions: List<BlockPos>
    fun isComplete(): Boolean

    fun cancel()
    fun notCancelled(): Boolean

    fun canTakeMoreWorkers(): Boolean
    fun addWorker()
    fun removeWorker()

}