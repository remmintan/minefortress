package net.remmintan.mods.minefortress.core.interfaces.resources

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.item.ItemStack
import net.remmintan.mods.minefortress.core.utils.extractItemsConsideringSimilar

@Suppress("UnstableApiUsage")
interface IResourceManager {

    fun getStorage(): Storage<ItemVariant>

    fun hasItems(stacks: List<ItemStack>): Boolean {
        val storage = getStorage()
        Transaction.openOuter().use { tr ->
            for (stack in stacks) {
                val itemVariant = ItemVariant.of(stack)
                val amountToExtract = stack.count.toLong()
                val extractedAmount = storage.extractItemsConsideringSimilar(itemVariant, amountToExtract, tr)
                if (extractedAmount != amountToExtract) {
                    return false
                }
            }
        }
        return true
    }


}