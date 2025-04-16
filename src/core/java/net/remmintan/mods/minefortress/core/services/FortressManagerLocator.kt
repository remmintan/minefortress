package net.remmintan.mods.minefortress.core.services

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import java.util.concurrent.ConcurrentHashMap

object FortressManagerLocator {

    private val servicesRegistry = ConcurrentHashMap<Class<Any>, (fortressPos: BlockPos, server: ServerWorld) -> Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T> register(cls: Class<T>, constructor: (fortressPos: BlockPos, server: ServerWorld) -> T) {
        servicesRegistry.put(cls as Class<Any>, constructor as (fortressPos: BlockPos, server: ServerWorld) -> Any)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(cls: Class<T>, fortressPos: BlockPos, world: ServerWorld?): T {
        world ?: error("No world whe getting the service from service loader")
        return servicesRegistry[cls as Class<Any>]?.let { it(fortressPos, world) as T }
            ?: error("Can't find any service of that type")
    }

}