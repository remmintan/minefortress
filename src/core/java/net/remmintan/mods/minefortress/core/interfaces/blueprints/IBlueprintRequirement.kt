package net.remmintan.mods.minefortress.core.interfaces.blueprints

import net.minecraft.item.Item

interface IBlueprintRequirement {

    val type: ProfessionType?
    val level: Int
    val totalLevels: Int

    fun getIcon(): Item? = type?.icon

    fun satisfies(type: ProfessionType?, level: Int): Boolean {
        if (type == null) return false
        return type == this.type && this.level >= level
    }

}