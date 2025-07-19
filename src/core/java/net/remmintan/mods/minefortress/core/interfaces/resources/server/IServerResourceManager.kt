package net.remmintan.mods.minefortress.core.interfaces.resources.server

import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.dtos.ItemInfo
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager

interface IServerResourceManager : IServerManager {
    fun hasItems(stacks: List<ItemInfo>): Boolean

    fun findContainerToPut(item: Item): BlockPos?
    fun findContainerToGet(item: Item): BlockPos?

}
