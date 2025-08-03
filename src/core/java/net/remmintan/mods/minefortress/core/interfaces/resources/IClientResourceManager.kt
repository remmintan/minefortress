package net.remmintan.mods.minefortress.core.interfaces.resources

import net.minecraft.item.ItemStack


interface IClientResourceManager {

    fun hasItems(stacks: List<ItemStack>): Boolean
    fun syncRequestedItems(stacks: List<ItemStack>)

}
