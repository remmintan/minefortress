package org.minefortress.blueprints.manager

import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintRequirementType
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintRequirement

class BlueprintRequirement(override val id: String) : IBlueprintRequirement {
    override val type: BlueprintRequirementType?
    override val level: Int

    init {
        var type: BlueprintRequirementType? = null
        var level = 0

        for (entry in BlueprintRequirementType.entries) {
            val requirementsIds = entry.requirementsIds
            val index = requirementsIds.indexOf(id)
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