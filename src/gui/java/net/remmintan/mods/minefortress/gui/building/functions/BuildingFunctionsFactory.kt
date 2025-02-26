package net.remmintan.mods.minefortress.gui.building.functions

import net.minecraft.client.font.TextRenderer
import net.remmintan.mods.minefortress.gui.building.handlers.IBuildingProvider

/**
 * Factory class to create building-specific function handlers
 */
object BuildingFunctionsFactory {

    private val functionHandlers = mutableMapOf<String, (IBuildingProvider, TextRenderer) -> IBuildingFunctions>()

    /**
     * Register a function handler for a specific building type
     */
    fun register(buildingId: String, factory: (IBuildingProvider, TextRenderer) -> IBuildingFunctions) {
        functionHandlers[buildingId] = factory
    }

    /**
     * Create a function handler for a specific building
     * @return the function handler or null if no handler is registered for the building type
     */
    fun create(provider: IBuildingProvider, textRenderer: TextRenderer): IBuildingFunctions? {
        val buildingId = provider.building.metadata.id
        return functionHandlers[buildingId]?.invoke(provider, textRenderer)
    }

    /**
     * Check if a building has registered functions
     */
    fun hasFunctions(buildingId: String): Boolean {
        return functionHandlers.containsKey(buildingId)
    }
} 