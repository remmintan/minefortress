package net.remmintan.mods.minefortress.core.services

import net.minecraft.util.math.BlockPos
import java.util.concurrent.ConcurrentHashMap

object FortressServiceLocator {

    private val servicesRegistry = ConcurrentHashMap<Class<Any>, (fortressPos: BlockPos) -> Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T> register(cls: Class<T>, constructor: (fortressPos: BlockPos) -> T) {
        servicesRegistry.put(cls as Class<Any>, constructor as (fortressPos: BlockPos) -> Any)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(cls: Class<T>, fortressPos: BlockPos): T {
        return servicesRegistry[cls as Class<Any>]?.let { it(fortressPos) as T }
            ?: error("Can't find any service of that type")
    }

}