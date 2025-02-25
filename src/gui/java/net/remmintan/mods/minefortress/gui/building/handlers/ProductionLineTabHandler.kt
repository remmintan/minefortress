package net.remmintan.mods.minefortress.gui.building.handlers

import net.minecraft.client.MinecraftClient
import net.remmintan.mods.minefortress.core.FortressGamemode
import net.remmintan.mods.minefortress.core.utils.CoreModUtils

class ProductionLineTabHandler(private val provider: IBuildingProvider) : IProductionLineTabHandler {

    override fun isCampfire(): Boolean {
        return provider.building.metadata.id == "campfire"
    }

    override fun switchToSurvivalMode() {
        CoreModUtils.getFortressManager().setGamemode(FortressGamemode.SURVIVAL)
        MinecraftClient.getInstance().setScreen(null)
    }
} 