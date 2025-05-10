package net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls

import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.tasks.IAreaBasedTask
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo

interface IAreaBasedTaskControl {

    fun setTask(task: IAreaBasedTask)
    fun hasTask(): Boolean
    fun hasMoreBlocks(): Boolean
    fun isWithinTheArea(): Boolean
    fun getAreaData(): Pair<BlockPos, Double>
    fun getCurrentBlock(): ITaskBlockInfo?
    fun moveToNextBlock(): ITaskBlockInfo?
    fun fail()

}