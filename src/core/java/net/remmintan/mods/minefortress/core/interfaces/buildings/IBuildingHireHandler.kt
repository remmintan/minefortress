package net.remmintan.mods.minefortress.core.interfaces.buildings

import net.remmintan.mods.minefortress.core.dtos.professions.HireProgressInfo
import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionHireInfo

interface IBuildingHireHandler {
    fun hire(professionId: String)
    fun getProfessions(): List<ProfessionHireInfo>
    fun getHireProgress(professionId: String): HireProgressInfo
}