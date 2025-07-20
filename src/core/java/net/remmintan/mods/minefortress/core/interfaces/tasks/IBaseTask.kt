package net.remmintan.mods.minefortress.core.interfaces.tasks

import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto

interface IBaseTask {

    fun getPos(): BlockPos
    fun toTaskInformationDto(): List<TaskInformationDto>
    fun isComplete(): Boolean

    fun cancel()
    fun notCancelled(): Boolean

    fun canTakeMoreWorkers(): Boolean
    fun addWorker()
    fun removeWorker()

}