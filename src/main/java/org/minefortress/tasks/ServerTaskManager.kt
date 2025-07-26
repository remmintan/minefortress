package org.minefortress.tasks

import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.blocks.FortressBlocks
import net.remmintan.mods.minefortress.blocks.task.FortressTaskBlockEntity
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider
import net.remmintan.mods.minefortress.core.interfaces.server.ITickableManager
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager
import net.remmintan.mods.minefortress.core.interfaces.tasks.*
import net.remmintan.mods.minefortress.core.utils.LogCompanion
import net.remmintan.mods.minefortress.core.utils.ServerModUtils
import net.remmintan.mods.minefortress.core.utils.getManagersProvider
import net.remmintan.mods.minefortress.core.utils.isSurvivalFortress
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper
import net.remmintan.mods.minefortress.networking.s2c.ClientboundTaskExecutedPacket
import net.remmintan.mods.minefortress.networking.s2c.S2CAddClientTasksPacket
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class ServerTaskManager(private val server: MinecraftServer, fortressPos: BlockPos) : IServerTaskManager,
    IWritableManager,
    ITickableManager {
    private val tasksInProgress: MutableMap<BlockPos, IBaseTask> = ConcurrentHashMap()
    private val notStartedTasks: Queue<IBaseTask> = LinkedList()

    private val managersProvider: IServerManagersProvider by lazy { server.getManagersProvider(fortressPos) }
    private val world: ServerWorld by lazy { server.overworld }

    override fun addTask(task: IBaseTask, selectedPawnIds: List<Int>, player: ServerPlayerEntity) {
        placeTaskInTheWorldAndReserveItems(task, player)

        //TODO place the task in the world + reserve items

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
        tasksInProgress[task.pos] = task
        setPawnsToTask(task, selectedWorkers)
    }

    override fun tick(server: MinecraftServer, world: ServerWorld, player: ServerPlayerEntity?) {
        removeAllFinishedTasks()

        //FIXME take planned tasks even when the player is offline
        if (player == null) return
        if (notStartedTasks.isEmpty() && tasksInProgress.isEmpty()) return
        val readyWorkers = ServerModUtils
            .getFortressManager(player)
            .map { it.readyWorkers }
            .orElse(emptyList())

        if (readyWorkers.isNotEmpty()) {
            val inProgressTask = tasksInProgress.values
                .firstOrNull { it.notCancelled() && it.canTakeMoreWorkers() }
                ?.also { setPawnsToTask(it, readyWorkers) }

            if (inProgressTask == null) {
                notStartedTasks.poll()?.let {
                    val selectedPawnIds = readyWorkers.map { w -> (w as Entity).id }
                    this.addTask(it, selectedPawnIds, player)
                }
            }
        }
    }

    override fun cancelTask(pos: BlockPos, player: ServerPlayerEntity) {
        val removedTask = tasksInProgress.remove(pos)
        removedTask?.cancel()
        val resourceManager = managersProvider.resourceManager
        val resourceHelper = managersProvider.resourceHelper
        if (!resourceHelper.transferItemsFromTask(resourceManager, pos)) {
            log.error("Couldn't return items from task at $pos to the resource manager")
        }
        world.removeBlock(pos, false)
    }

    private fun removeAllFinishedTasks() {
        val finishedTasks = tasksInProgress.filterValues { it.isComplete() }.keys
        finishedTasks.forEach {
            tasksInProgress.remove(it)
            world.removeBlock(it, false)
        }
    }

    private fun setPawnsToTask(task: IBaseTask, workers: List<IWorkerPawn>) {
        when (task) {
            is ITask ->
                for (worker in workers) {
                    if (!task.hasAvailableParts() || !task.canTakeMoreWorkers()) break
                    task.addWorker()
                    worker.taskControl.setTask(task)
                }

            is IAreaBasedTask ->
                for (worker in workers) {
                    if (!task.hasMoreBlocks() || !task.canTakeMoreWorkers()) break
                    task.addWorker()
                    worker.areaBasedTaskControl.setTask(task)
                }

            else -> error("Wrong task class")
        }
    }


    override fun write(tag: NbtCompound) {
    }

    override fun read(tag: NbtCompound) {
    }

    private fun placeTaskInTheWorldAndReserveItems(task: IBaseTask, player: ServerPlayerEntity) {
        val taskInformationDto = task.toTaskInformationDto()

        val taskPos = task.pos
        if (world.getBlockEntity(taskPos) !is FortressTaskBlockEntity) {
            world.setBlockState(taskPos, FortressBlocks.FORTRESS_TASK.defaultState)
            val success = reserveItems(task, taskPos)
            if (!success) {
                FortressServerNetworkHelper.send(
                    player,
                    FortressChannelNames.FINISH_TASK,
                    ClientboundTaskExecutedPacket(taskPos)
                )
            }
        }

        val packet = S2CAddClientTasksPacket(taskInformationDto)
        FortressServerNetworkHelper.send(player, S2CAddClientTasksPacket.CHANNEL, packet)
    }

    private fun reserveItems(task: IBaseTask, taskPos: BlockPos): Boolean {
        if (!server.isSurvivalFortress()) return true

        val resourceHelper = managersProvider.resourceHelper
        val resourceManager = managersProvider.resourceManager
        val success = when (task) {
            is SimpleSelectionTask -> {
                task.placingItem?.let {
                    val item = it
                    val amount = task.positions.size
                    val stack = ItemStack(item, amount)
                    resourceHelper.transferItemsToTask(
                        resourceManager,
                        taskPos,
                        listOf(stack)
                    )
                } ?: true
            }

            is AreaBlueprintTask -> {
                resourceHelper.transferItemsToTask(
                    resourceManager,
                    taskPos,
                    task.requiredResources
                )
            }

            is RepairBuildingTask -> {
                resourceHelper.transferItemsToTask(
                    resourceManager,
                    taskPos,
                    task.repairItems
                )
            }

            else -> true
        }

        if (!success) {
            log.error("Was not able to reserve items for task $taskPos. Seemingly not enough items")
        }

        return success
    }

    companion object : LogCompanion(ServerTaskManager::class) {
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
