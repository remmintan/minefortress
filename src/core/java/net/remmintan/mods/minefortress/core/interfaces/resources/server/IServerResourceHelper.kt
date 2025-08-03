package net.remmintan.mods.minefortress.core.interfaces.resources.server

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager

@Suppress("UnstableApiUsage")
interface IServerResourceHelper : IServerManager {

    fun putItemToSuitableContainer(stack: ItemStack) {
        putItemsToSuitableContainer(listOf(stack))
    }
    fun putItemsToSuitableContainer(stacks: List<ItemStack>): Boolean

    fun transferItemsToTask(resourceManager: IServerResourceManager, taskPos: BlockPos, items: List<ItemStack>): Boolean
    fun transferItemsFromTask(resourceManager: IServerResourceManager, taskPos: BlockPos): Boolean

    fun payItemFromTask(taskPos: BlockPos, item: Item, canIgnore: Boolean)
    fun payItems(from: Storage<ItemVariant>, items: List<ItemStack>): Boolean

    fun getCountIncludingSimilar(item: Item): Long
    fun syncRequestedItems(items: Set<Item>, player: ServerPlayerEntity)

}