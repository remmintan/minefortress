package org.minefortress.fortress.resources.server

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.resources.server.IServerResourceHelper
import net.remmintan.mods.minefortress.core.interfaces.resources.server.IServerResourceManager
import net.remmintan.mods.minefortress.core.utils.LogCompanion
import net.remmintan.mods.minefortress.core.utils.SimilarItemsHelper
import net.remmintan.mods.minefortress.core.utils.extractItemsConsideringSimilar
import net.remmintan.mods.minefortress.core.utils.getManagersProvider
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper
import net.remmintan.mods.minefortress.networking.s2c.S2CSyncItemsState

@Suppress("UnstableApiUsage")
class ServerResourceHelper(server: MinecraftServer, fortressPos: BlockPos) : IServerResourceHelper {

    private val resourceManager: IServerResourceManager by lazy { server.getManagersProvider(fortressPos).resourceManager }
    private val world: ServerWorld by lazy { server.overworld }

    override fun putItemsToSuitableContainer(stacks: List<ItemStack>): Boolean {
        val storage = resourceManager.getStorage()
        Transaction.openOuter().use { tr ->
            for (stack in stacks) {
                val itemVariant = ItemVariant.of(stack)
                val amountToInsert = stack.count.toLong()

                val insertedAmount = storage.insert(itemVariant, amountToInsert, tr)
                if (amountToInsert != insertedAmount)
                    return false
            }
            tr.commit()
        }
        return true
    }

    override fun transferItemsToTask(
        resourceManager: IServerResourceManager,
        taskPos: BlockPos,
        items: List<ItemStack>
    ): Boolean {
        val resourceManagerStorage = resourceManager.getStorage()
        val taskStorage = ItemStorage.SIDED.find(world, taskPos, null)
        if (taskStorage == null || !taskStorage.supportsInsertion()) {
            log.error("Trying to transfer items to task but the task inventory is invalid")
            return false
        }

        return transferItems(items, resourceManagerStorage, taskStorage)
    }

    override fun transferItemsFromTask(resourceManager: IServerResourceManager, taskPos: BlockPos): Boolean {
        val resourceManagerStorage = resourceManager.getStorage()
        val taskStorage = ItemStorage.SIDED.find(world, taskPos, null)
        if (taskStorage == null || !taskStorage.supportsInsertion()) {
            log.error("Trying to transfer items from task but the task inventory is invalid")
            return false
        }

        val totalItemsAmount = taskStorage.sumOf { it.amount }

        Transaction.openOuter().use { tr ->
            val movedItems =
                StorageUtil.move(taskStorage, resourceManagerStorage, { variant -> true }, totalItemsAmount, tr)
            if (totalItemsAmount != movedItems) {
                return false
            }
            tr.commit()
        }
        return true
    }

    override fun payItemFromTask(taskPos: BlockPos, item: Item, canIgnore: Boolean) {
        val taskStorage = ItemStorage.SIDED.find(world, taskPos, null)
        if (taskStorage == null || !taskStorage.supportsInsertion()) {
            log.error("Trying to remove $item from task but the task inventory is invalid")
            return
        }

        Transaction.openOuter().use { tr ->
            val variant = ItemVariant.of(item)
            val extractedAmount = taskStorage.extractItemsConsideringSimilar(variant, 1, tr)
            if (extractedAmount == 1L) {
                tr.commit()
            } else {
                if (!canIgnore)
                    log.warn("Trying to remove the item $item from a task $taskPos. But there is no such resource left in the task storage")
            }
        }
    }

    override fun payItems(from: Storage<ItemVariant>, items: List<ItemStack>): Boolean {
        Transaction.openOuter().use { tr ->
            for (stack in items) {
                val item = ItemVariant.of(stack)
                val amountToExtract = stack.count.toLong()
                val extractedAmount = from.extractItemsConsideringSimilar(item, amountToExtract, tr)
                if (amountToExtract != extractedAmount)
                    return false
            }
            tr.commit()
        }
        return true
    }

    override fun getCountIncludingSimilar(item: Item): Long {
        val storage = resourceManager.getStorage()
        val items = SimilarItemsHelper.getSimilarItems(item).toSet() + item
        var totalCount = 0L
        for (view in storage) {
            if (items.contains(view.resource.item))
                totalCount += view.amount
        }

        return totalCount
    }

    override fun syncRequestedItems(items: Set<Item>, player: ServerPlayerEntity) {
        val requestedStates = items.map {
            val count = getCountIncludingSimilar(it)
            ItemStack(it, count.toInt())
        }
        val packet = S2CSyncItemsState(requestedStates)
        FortressServerNetworkHelper.send(player, S2CSyncItemsState.CHANNEL, packet)
    }

    private fun transferItems(
        items: List<ItemStack>,
        fromStorage: Storage<ItemVariant>,
        toStorage: Storage<ItemVariant>
    ): Boolean {
        Transaction.openOuter().use { tr ->
            for (stack in items) {
                val item = ItemVariant.of(stack)
                val amountToTransfer = stack.count.toLong()

                val extractedAmount = fromStorage.extractItemsConsideringSimilar(item, amountToTransfer, tr)
                val insertedAmount = toStorage.insert(item, amountToTransfer, tr)
                if (extractedAmount != amountToTransfer || insertedAmount != amountToTransfer)
                    return false
            }

            tr.commit()
        }
        return true
    }

    companion object : LogCompanion(ServerResourceHelper::class)
}