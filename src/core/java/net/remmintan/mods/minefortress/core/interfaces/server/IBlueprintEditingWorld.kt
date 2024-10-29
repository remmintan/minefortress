package net.remmintan.mods.minefortress.core.interfaces.server

import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup

interface IBlueprintEditingWorld {
    var blueprintId: String?
    var blueprintName: String?
    var floorLevel: Int
    var blueprintGroup: BlueprintGroup?

    fun enableSaveStructureMode()

    fun disableSaveStructureMode()
}
