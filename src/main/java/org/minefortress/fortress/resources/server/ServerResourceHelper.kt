package org.minefortress.fortress.resources.server

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.resources.server.IServerResourceHelper
import net.remmintan.mods.minefortress.core.interfaces.resources.server.IServerResourceManager
import net.remmintan.mods.minefortress.core.utils.LogCompanion
import net.remmintan.mods.minefortress.core.utils.getManagersProvider

@Suppress("UnstableApiUsage")
class ServerResourceHelper(server: MinecraftServer, fortressPos: BlockPos) : IServerResourceHelper {

    private val resourceManager: IServerResourceManager by lazy { server.getManagersProvider(fortressPos)!!.resourceManager }
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
            log.error("Trying to transfer items to task but the task inventory is invalid")
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

    private fun transferItems(
        items: List<ItemStack>,
        fromStorage: Storage<ItemVariant>,
        toStorage: Storage<ItemVariant>
    ): Boolean {
        Transaction.openOuter().use { tr ->
            for (stack in items) {
                val item = ItemVariant.of(stack)
                val amountToTransfer = stack.count.toLong()

                val extractedAmount = fromStorage.extract(item, amountToTransfer, tr)
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