package org.minefortress.tasks

import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider
import net.remmintan.mods.minefortress.core.interfaces.server.ITickableManager
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager
import net.remmintan.mods.minefortress.core.interfaces.tasks.*
import net.remmintan.mods.minefortress.core.utils.ServerModUtils
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper
import net.remmintan.mods.minefortress.networking.s2c.S2CAddClientTasksPacket
import java.util.*


class ServerTaskManager : IServerTaskManager, IWritableManager, ITickableManager {
    private val nonFinishedTasks: MutableMap<UUID, IBaseTask> = HashMap()
    private val notStartedTasks: Queue<IBaseTask> = LinkedList()

    override fun addTask(task: IBaseTask, selectedPawnIds: List<Int>, player: ServerPlayerEntity) {
        removeAllFinishedTasks()

        val packet = S2CAddClientTasksPacket(task.toTaskInformationDto())
        FortressServerNetworkHelper.send(player, S2CAddClientTasksPacket.CHANNEL, packet)
        nonFinishedTasks[task.getId()] = task

        if (selectedPawnIds.isEmpty()) {
            notStartedTasks.add(task)
            return
        }

        val selectedWorkers = filterWorkers(selectedPawnIds, player)
        if (selectedWorkers.isEmpty()) {
            notStartedTasks.add(task)
            return
        }

        if (task is ITaskWithPreparation)
            task.prepareTask()
        setPawnsToTask(task, selectedWorkers)
    }

    override fun tick(server: MinecraftServer, world: ServerWorld, player: ServerPlayerEntity?) {
        //FIXME take planned tasks even when the player is offline
        if (player == null) return
        if (notStartedTasks.isEmpty()) return
        val freeWorkers = ServerModUtils
            .getFortressManager(player)
            .map { it.freeWorkers }
            .orElse(emptyList())
        if (freeWorkers.size > 2) {
            val task = notStartedTasks.remove()
            val freeWorkersIds = freeWorkers.map { it: IWorkerPawn -> (it as Entity).id }.toList()
            this.addTask(task, freeWorkersIds, player)
        }
    }

    override fun cancelTask(id: UUID, player: ServerPlayerEntity) {
        removeAllFinishedTasks()
        val removedTask = nonFinishedTasks.remove(id)
        removedTask?.cancel()
        ServerModUtils.getManagersProvider(player).ifPresent { it: IServerManagersProvider ->
            it.resourceManager.returnReservedItems(id)
        }
    }

    private fun removeAllFinishedTasks() {
        val finishedTasks = nonFinishedTasks.filterValues { it.isComplete() }.keys
        finishedTasks.forEach { nonFinishedTasks.remove(it) }
    }

    private fun setPawnsToTask(task: IBaseTask, workers: List<IWorkerPawn>) {
        when (task) {
            is ITask ->
                for (worker in workers) {
                    if (!task.hasAvailableParts()) break
                    worker.taskControl.setTask(task)
                }

            is IAreaBasedTask ->
                for (worker in workers) {
                    if (!task.hasMoreBlocks()) break
                    worker.areaBasedTaskControl.setTask(task)
                }

            else -> error("Wrong task class")
        }
    }


    override fun write(tag: NbtCompound) {
    }

    override fun read(tag: NbtCompound) {
    }

    companion object {
        private fun filterWorkers(selectedPawnIds: List<Int>, player: ServerPlayerEntity): List<IWorkerPawn> {
            val serverWorld = player.world
            return selectedPawnIds
                .asSequence()
                .map { serverWorld.getEntityById(it) }
                .filterNotNull()
                .filter { it is IWorkerPawn }
                .map { it as IWorkerPawn }
                .filter { !it.taskControl.isDoingEverydayTasks }
                .toList()
        }
    }
}
