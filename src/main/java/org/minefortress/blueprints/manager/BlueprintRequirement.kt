package org.minefortress.blueprints.manager

import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintRequirement
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType

class BlueprintRequirement(blueprintId: String) : IBlueprintRequirement {
    override val type: ProfessionType?
    override val level: Int

    init {
        var type: ProfessionType? = null
        var level = -1

        for (entry in ProfessionType.entries) {
            val requiredBlueprints = entry.blueprintIds
            val index = requiredBlueprints.indexOf(blueprintId)
            if (index >= 0) {
                type = entry
                level = index
                break
            }
        }

        this.type = type
        this.level = level
    }


}