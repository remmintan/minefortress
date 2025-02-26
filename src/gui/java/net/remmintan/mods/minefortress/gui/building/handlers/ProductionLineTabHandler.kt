package net.remmintan.mods.minefortress.gui.building.handlers

import net.minecraft.client.font.TextRenderer
import net.remmintan.mods.minefortress.gui.building.functions.BuildingFunctionsFactory
import net.remmintan.mods.minefortress.gui.building.functions.IBuildingFunctions

class ProductionLineTabHandler(private val provider: IBuildingProvider) : IProductionLineTabHandler {

    override fun hasFunctions(): Boolean {
        val buildingId = provider.building.metadata.id
        return BuildingFunctionsFactory.hasFunctions(buildingId)
    }

    override fun getBuildingFunctions(textRenderer: TextRenderer): IBuildingFunctions? {
        return BuildingFunctionsFactory.create(provider, textRenderer)
    }
} 