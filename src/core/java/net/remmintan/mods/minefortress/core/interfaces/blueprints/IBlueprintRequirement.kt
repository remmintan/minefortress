package net.remmintan.mods.minefortress.core.interfaces.blueprints

import net.minecraft.item.Item

interface IBlueprintRequirement {

    val id: String
    val type: BlueprintRequirementType?
    val level: Int

    fun getIcon(): Item? = type?.icon

}