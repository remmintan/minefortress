package net.remmintan.mods.minefortress.core.interfaces.buildings

import net.remmintan.mods.minefortress.core.dtos.professions.HireProgressInfo

interface IBuildingHireHandler {
    fun hire(professionId: String)
    fun getProfessionIds(): List<String>
    fun getHireProgress(professionId: String): HireProgressInfo
}