package net.remmintan.mods.minefortress.core.interfaces.resources.server

import net.minecraft.util.math.BlockPos

interface IServerContainersRegistry {

    fun register(pos: BlockPos)
    fun unregister(pos: BlockPos)

}