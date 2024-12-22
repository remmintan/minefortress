package net.remmintan.mods.minefortress.gui.building.handlers

import net.minecraft.item.ItemStack
import net.remmintan.mods.minefortress.core.dtos.ItemInfo
import net.remmintan.mods.minefortress.networking.c2s.C2SHireProfessional
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper

class WorkforceTabHandler(private val provider: IBuildingProvider) : IWorkforceTabHandler {

    private val handler by lazy { provider.building.hireHandler }
    private val professions by lazy { handler.getProfessions().associateBy { it.professionId } }

    override fun getProfessions(): List<String> = professions.keys.toList()

    override fun getCost(professionId: String): List<ItemInfo> {
        return professions.getValue(professionId).professionCost
    }

    override fun getProfessionItem(professionId: String): ItemStack {
        return professions.getValue(professionId).professionItem
    }

    override fun getProfessionName(professionId: String): String {
        return professions.getValue(professionId).professionName
    }

    override fun getHireProgress(professionId: String): Int {
        return handler.getHireProgress(professionId).progress
    }

    override fun getHireQueue(professionId: String): Int {
        return handler.getHireProgress(professionId).queueLength
    }

    override fun getCurrentCount(professionId: String): Int {
        return handler.getHireProgress(professionId).currentCount
    }

    override fun getMaxCount(professionId: String): Int {
        return handler.getHireProgress(professionId).maxCount
    }

    override fun increaseAmount(professionId: String) {
        val pos = provider.building.pos
        val packet = C2SHireProfessional(pos, professionId)
        FortressClientNetworkHelper.send(C2SHireProfessional.CHANNEL, packet)
    }

    override fun canIncreaseAmount(costs: List<ItemInfo>, professionId: String): Boolean {
        return handler.getHireProgress(professionId).canHireMore
    }


}