package org.minefortress.fortress.resources.client

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.remmintan.mods.minefortress.core.interfaces.resources.IClientResourceHelper
import net.remmintan.mods.minefortress.core.interfaces.resources.IClientResourceManager
import net.remmintan.mods.minefortress.core.utils.ClientModUtils
import net.remmintan.mods.minefortress.core.utils.SimilarItemsHelper
import net.remmintan.mods.minefortress.core.utils.extractItemsConsideringSimilar

@Suppress("UnstableApiUsage")
class ClientResourceHelper : IClientResourceHelper {

    override fun getMetRequirements(costs: List<ItemStack>): Map<ItemStack, Boolean> {
        val storage = getResourceManager().getStorage()
        Transaction.openOuter().use { tr ->
            return costs.associateWith {
                val itemVariant = ItemVariant.of(it)
                val amountToExtract = it.count.toLong()
                val extractedAmount = storage.extractItemsConsideringSimilar(itemVariant, amountToExtract, tr)
                amountToExtract == extractedAmount
            }
        }
    }

    override fun getCountIncludingSimilar(item: Item): Long {
        val storage = getResourceManager().getStorage()
        val items = SimilarItemsHelper.getSimilarItems(item).toSet() + item
        var totalCount = 0L
        for (view in storage) {
            if (items.contains(view.resource.item))
                totalCount += view.amount
        }

        return totalCount
    }

    private fun getResourceManager(): IClientResourceManager {
        return ClientModUtils.getFortressManager().resourceManager
    }

}