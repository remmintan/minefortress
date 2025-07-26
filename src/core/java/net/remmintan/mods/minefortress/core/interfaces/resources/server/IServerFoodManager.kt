package net.remmintan.mods.minefortress.core.interfaces.resources.server

import net.minecraft.item.ItemStack
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager

interface IServerFoodManager : IServerManager {

    fun hasFood(): Boolean
    fun getFood(): ItemStack?

}