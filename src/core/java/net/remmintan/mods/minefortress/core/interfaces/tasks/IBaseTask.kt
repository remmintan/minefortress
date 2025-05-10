package net.remmintan.mods.minefortress.core.interfaces.tasks

import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto
import java.util.*

interface IBaseTask {

    fun getId(): UUID
    fun toTaskInformationDto(): List<TaskInformationDto>
    fun isComplete(): Boolean

    fun cancel()
    fun notCancelled(): Boolean

}