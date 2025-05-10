package net.remmintan.mods.minefortress.core.interfaces.tasks

import net.minecraft.server.network.ServerPlayerEntity
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager
import java.util.*

interface IServerTaskManager : IServerManager {
    fun addTask(task: IBaseTask, selectedPawnIds: List<Int>, player: ServerPlayerEntity)
    fun cancelTask(id: UUID, player: ServerPlayerEntity)
}
