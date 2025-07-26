package net.remmintan.mods.minefortress.core.interfaces.tasks

import com.mojang.datafixers.util.Pair
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.TaskType
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn

interface ITask : IBaseTask {
    val taskType: TaskType
    fun hasAvailableParts(): Boolean
    fun getNextPart(colonist: IWorkerPawn): ITaskPart?
    fun returnPart(partStartAndEnd: Pair<BlockPos, BlockPos>)
    fun finishPart(part: ITaskPart, colonist: IWorkerPawn)
}
