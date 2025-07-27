package net.remmintan.mods.minefortress.core.interfaces.resources.server

import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager

interface IServerContainersRegistry : IServerManager {

    fun register(pos: BlockPos)
    fun unregister(pos: BlockPos)

}