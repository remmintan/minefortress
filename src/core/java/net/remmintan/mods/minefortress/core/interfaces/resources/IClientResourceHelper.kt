package net.remmintan.mods.minefortress.core.interfaces.resources

import net.minecraft.item.ItemStack

interface IClientResourceHelper {

    fun getMetRequirements(costs: List<ItemStack>): Map<ItemStack, Boolean>

}