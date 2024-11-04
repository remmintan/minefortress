package net.remmintan.mods.minefortress.core.dtos.buildings

import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType

class BlueprintRequirement(blueprintId: String) {
    val type: ProfessionType?
    val level: Int
    val totalLevels: Int
    val upgrades: List<String>

    init {
        var type: ProfessionType? = null
        var level = -1
        var totalLevels = 0
        val upgrades = mutableListOf<String>()

        for (entry in ProfessionType.entries) {
            val requiredBlueprints = entry.blueprintIds
            val index = requiredBlueprints.indexOf(blueprintId)
            if (index >= 0) {
                type = entry
                level = index
                totalLevels = requiredBlueprints.size

                requiredBlueprints.stream().skip(index + 1L).forEach { upgrades.add(it) }

                break
            }
        }

        this.type = type
        this.level = level
        this.totalLevels = totalLevels
        this.upgrades = upgrades
    }

    val icon = type?.icon

    fun satisfies(type: ProfessionType?, level: Int): Boolean {
        if (type == null) return false
        return type == this.type && this.level >= level
    }


}