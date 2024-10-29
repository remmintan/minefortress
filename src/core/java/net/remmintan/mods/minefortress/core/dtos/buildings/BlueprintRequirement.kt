package net.remmintan.mods.minefortress.core.dtos.buildings

import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType

class BlueprintRequirement(blueprintId: String) {
    val type: ProfessionType?
    val level: Int
    val totalLevels: Int

    init {
        var type: ProfessionType? = null
        var level = -1
        var totalLevels = 0

        for (entry in ProfessionType.entries) {
            val requiredBlueprints = entry.blueprintIds
            val index = requiredBlueprints.indexOf(blueprintId)
            if (index >= 0) {
                type = entry
                level = index
                totalLevels = requiredBlueprints.size
                break
            }
        }

        this.type = type
        this.level = level
        this.totalLevels = totalLevels
    }

    val icon = type?.icon

    fun satisfies(type: ProfessionType?, level: Int): Boolean {
        if (type == null) return false
        return type == this.type && this.level >= level
    }


}