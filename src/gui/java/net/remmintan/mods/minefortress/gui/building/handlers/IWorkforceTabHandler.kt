package net.remmintan.mods.minefortress.gui.building.handlers

import net.minecraft.item.ItemStack
import net.remmintan.mods.minefortress.core.dtos.ItemInfo

interface IWorkforceTabHandler {

    fun getProfessions(): List<String>

    fun getCost(professionId: String?): List<ItemInfo>
    fun getHireProgress(professionId: String): Int
    fun getHireQueue(professionId: String): Int
    fun getCurrentCount(professionId: String): Int
    fun getMaxCount(professionId: String): Int
    fun increaseAmount(professionId: String)
    fun getProfessionItem(professionId: String): ItemStack
    fun getProfessionName(professionId: String): String
    fun canIncreaseAmount(costs: List<ItemInfo>, professionId: String): Boolean

}