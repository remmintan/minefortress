package net.remmintan.mods.minefortress.core.dtos.buildings

import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintRequirement
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType

class BlueprintRequirement(blueprintId: String) : IBlueprintRequirement {
    override val type: ProfessionType?
    override val level: Int
    override val totalLevels: Int
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

}