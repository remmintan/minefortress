package net.remmintan.mods.minefortress.core.interfaces.tasks

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager

interface IServerTaskManager : IServerManager {
    fun addTask(task: IBaseTask, selectedPawnIds: List<Int>, player: ServerPlayerEntity)
    fun cancelTask(pos: BlockPos, player: ServerPlayerEntity)
}
