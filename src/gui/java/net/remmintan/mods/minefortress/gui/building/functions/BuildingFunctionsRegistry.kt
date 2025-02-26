package net.remmintan.mods.minefortress.gui.building.functions

/**
 * Registry for all building functions
 */
object BuildingFunctionsRegistry {

    /**
     * Register all building functions
     */
    fun register() {
        // Register Campfire functions
        BuildingFunctionsFactory.register("campfire") { _, _ -> CampfireFunctions() }

        // Register other building functions here
        // Example:
        // BuildingFunctionsFactory.register("sawmill") { provider, textRenderer ->
        //     SawmillFunctions(provider as IProductionLineTabHandler, textRenderer)
        // }
    }
} 