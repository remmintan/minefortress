package net.remmintan.mods.minefortress.gui.building.handlers

import net.minecraft.item.ItemStack
import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionCost
import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionHireInfo
import net.remmintan.mods.minefortress.core.utils.ClientModUtils
import net.remmintan.mods.minefortress.networking.c2s.C2SHireProfessional
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper

class WorkforceTabHandler(private val provider: IBuildingProvider) : IWorkforceTabHandler {

    private val handler by lazy { provider.building.hireHandler }
    private val professions: Map<String, ProfessionHireInfo>
        get() {
            val fortressManager = ClientModUtils.getFortressManager()
            val buildingScreenInfoService = fortressManager.clientBuildingScreenInfoService
            return buildingScreenInfoService.getProfessions().associateBy { it.professionId }
        }

    override fun getProfessions(): List<String> = professions.keys.toList()

    override fun getCost(professionId: String): List<ProfessionCost> {
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
        val maxCount = handler.getHireProgress(professionId).maxCount

        val bookedCapacity = getProfessions().filter { it != professionId }.sumOf {
            this.getCurrentCount(it) + this.getHireQueue(it)
        }

        return maxCount - bookedCapacity
    }

    override fun increaseAmount(professionId: String) {
        // All professions now use the hire menu approach
        val pos = provider.building.pos
        val packet = C2SHireProfessional(pos, professionId)
        FortressClientNetworkHelper.send(C2SHireProfessional.CHANNEL, packet)
    }

    override fun canHireMore(professionId: String): Boolean {
        return handler.getHireProgress(professionId).canHireMore
    }

    override fun getAvailablePawns(): Int {
        return ClientModUtils.getProfessionManager().freeColonists
    }
}
