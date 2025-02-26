package net.remmintan.mods.minefortress.gui.building.handlers

import net.minecraft.client.font.TextRenderer
import net.remmintan.mods.minefortress.gui.building.functions.IBuildingFunctions

interface IProductionLineTabHandler {
    /**
     * Checks if the building has any production functions available
     */
    fun hasFunctions(): Boolean

    /**
     * Get the building functions for the current building
     */
    fun getBuildingFunctions(textRenderer: TextRenderer): IBuildingFunctions?

} 