package org.minefortress.blueprints.manager

import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintRequirement
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType

class BlueprintRequirement(blueprintId: String) : IBlueprintRequirement {
    override val type: ProfessionType?
    override val level: Int
    override val totalLevels: Int

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


}