package org.minefortress.entity.ai.controls

import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.IAreaBasedTaskControl
import net.remmintan.mods.minefortress.core.interfaces.tasks.IAreaBasedTask
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo
import org.minefortress.entity.Colonist
import kotlin.math.sqrt

class AreaBasedTaskControl(private val pawn: Colonist) : IAreaBasedTaskControl {

    private var task: IAreaBasedTask? = null

    private var currentBlock: ITaskBlockInfo? = null

    override fun setTask(task: IAreaBasedTask) {
        this.task = task
    }

    override fun hasTask() = task != null
    override fun hasMoreBlocks() = task
        ?.let { it.notCancelled() && it.hasMoreBlocks() }
        ?: false

    override fun isWithinTheArea(): Boolean {
        return task?.let {
            val (center, r) = it.areaData
            sqrt(pawn.pos.squaredDistanceTo(center.toCenterPos())) <= r
        } ?: false
    }

    override fun getAreaData(): Pair<BlockPos, Double> {
        return task?.areaData ?: Pair(BlockPos.ORIGIN, 0.0)
    }

    override fun moveToNextBlock(): ITaskBlockInfo? {
        currentBlock?.let { task?.successBlock(it.pos) }
        currentBlock = task?.getNextBlock()
        if (task?.isComplete() == true) {
            task?.onCompletion(pawn)
        }

        return currentBlock
    }

    override fun getCurrentBlock() = currentBlock

    override fun reset() {
        currentBlock?.let { task?.failBlock(it) }
        this.task = null
        this.currentBlock = null
    }


}