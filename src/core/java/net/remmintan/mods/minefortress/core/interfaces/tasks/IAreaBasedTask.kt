package net.remmintan.mods.minefortress.core.interfaces.tasks

import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity

interface IAreaBasedTask : IBaseTask {

    val areaData: Pair<BlockPos, Double>
    fun getNextBlock(): ITaskBlockInfo?
    fun failBlock(info: ITaskBlockInfo)
    fun successBlock(pos: BlockPos)
    fun hasMoreBlocks(): Boolean
    fun onCompletion(worker: IFortressAwareEntity) {}


}