package org.minefortress.fortress.resources.server

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.resources.server.IServerResourceHelper
import net.remmintan.mods.minefortress.core.interfaces.resources.server.IServerResourceManager
import net.remmintan.mods.minefortress.core.utils.LogCompanion
import net.remmintan.mods.minefortress.core.utils.getManagersProvider

@Suppress("UnstableApiUsage")
class ServerResourceHelper(server: MinecraftServer, fortressPos: BlockPos) : IServerResourceHelper {

    private val resourceManager: IServerResourceManager by lazy { server.getManagersProvider(fortressPos)!!.resourceManager }

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

    companion object : LogCompanion(ServerResourceHelper::class)
}