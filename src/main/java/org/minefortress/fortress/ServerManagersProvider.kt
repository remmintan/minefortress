package org.minefortress.fortress

import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.automation.server.IServerAutomationAreaManager
import net.remmintan.mods.minefortress.core.interfaces.buildings.IServerBuildingsManager
import net.remmintan.mods.minefortress.core.interfaces.combat.IServerFightManager
import net.remmintan.mods.minefortress.core.interfaces.professions.IServerProfessionsManager
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager
import net.remmintan.mods.minefortress.core.interfaces.server.*
import net.remmintan.mods.minefortress.core.interfaces.tasks.IServerTaskManager
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksCreator
import net.remmintan.mods.minefortress.core.utils.getFortressOwner
import org.minefortress.fight.ServerFightManager
import org.minefortress.fortress.automation.areas.AreasServerManager
import org.minefortress.fortress.buildings.FortressBuildingManager
import org.minefortress.fortress.resources.server.ServerResourceManager
import org.minefortress.professions.ServerProfessionManager
import org.minefortress.tasks.ServerTaskManager
import org.minefortress.tasks.TasksCreator

class ServerManagersProvider(private val fortressPos: BlockPos, world: ServerWorld) : IServerManagersProvider {

    private val managers = mutableMapOf<Class<out IServerManager>, IServerManager>()

    init {
        registerManager(IServerTaskManager::class.java, ServerTaskManager())
        registerManager(IServerProfessionsManager::class.java, ServerProfessionManager(fortressPos, world))
        registerManager(IServerResourceManager::class.java, ServerResourceManager(world.server))
        registerManager(IServerBuildingsManager::class.java, FortressBuildingManager(fortressPos, world))
        registerManager(IServerAutomationAreaManager::class.java, AreasServerManager())
        registerManager(IServerFightManager::class.java, ServerFightManager(fortressPos))
        registerManager(ITasksCreator::class.java, TasksCreator())
    }

    private fun registerManager(managerInterface: Class<out IServerManager>, manager: IServerManager) {
        if (!managerInterface.isAssignableFrom(manager.javaClass))
            error("Wrong manager interface or manager")
        managers[managerInterface] = manager
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : IServerManager> getManager(managerClass: Class<T>): T {
        val serverManager = managers[managerClass] ?: error("Can't find manager class")
        return serverManager as T
    }

    override fun tick(server: MinecraftServer?, world: ServerWorld?) {
        if (server == null || world == null) return
        val fortressOwner = server.getFortressOwner(fortressPos)
        for (manager in managers.values) {
            if (manager is ITickableManager) {
                manager.tick(server, world, fortressOwner)
            }
        }
    }

    override fun sync() {
        for (manager in managers.values) {
            if (manager is ISyncableServerManager) {
                manager.sync()
            }
        }
    }

    override fun write(tag: NbtCompound?) {
        for (manager in managers.values) {
            if (manager is IWritableManager) {
                manager.write(tag)
            }
        }
    }

    override fun read(tag: NbtCompound?) {
        for (manager in managers.values) {
            if (manager is IWritableManager) {
                manager.read(tag)
            }
        }

        this.sync()
    }
}