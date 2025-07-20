package net.remmintan.mods.minefortress.core.interfaces.resources.server

import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager

interface IServerResourceHelper : IServerManager {

    fun putItemsToSuitableContainer(stacks: List<ItemStack>): Boolean

    fun transferItemsToTask(resourceManager: IServerResourceManager, taskPos: BlockPos, items: List<ItemStack>): Boolean
    fun transferItemsFromTask(resourceManager: IServerResourceManager, taskPos: BlockPos): Boolean

}