package org.minefortress.professions

import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintRequirement
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType

class ProfessionBlueprintRequirement(
    override val type: ProfessionType?, override val level: Int,
    override val totalLevels: Int
) : IBlueprintRequirement