package net.remmintan.mods.minefortress.gui.building.handlers

import net.minecraft.item.ItemStack
import net.remmintan.mods.minefortress.core.dtos.ItemInfo

class WorkforceTabHandler(private val provider: IBuildingProvider) : IWorkforceTabHandler {

    private val building by lazy { provider.building }

    override fun getProfessions(): List<String> {
        TODO("Not yet implemented")
    }

    override fun getCost(professionId: String?): List<ItemInfo> {
        TODO("Not yet implemented")
    }

    override fun getHireProgress(professionId: String): Int {
        TODO("Not yet implemented")
    }

    override fun getHireQueue(professionId: String): Int {
        TODO("Not yet implemented")
    }

    override fun getCurrentCount(professionId: String): Int {
        TODO("Not yet implemented")
    }

    override fun getMaxCount(professionId: String): Int {
        TODO("Not yet implemented")
    }

    override fun increaseAmount(professionId: String) {
        TODO("Not yet implemented")
    }

    override fun getProfessionItem(professionId: String): ItemStack {
        TODO("Not yet implemented")
    }

    override fun getProfessionName(professionId: String): String {
        TODO("Not yet implemented")
    }

    override fun canIncreaseAmount(costs: List<ItemInfo>, professionId: String): Boolean {
        TODO("Not yet implemented")
    }


}