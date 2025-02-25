package net.remmintan.mods.minefortress.gui.building.handlers

import net.minecraft.client.MinecraftClient
import net.minecraft.world.GameMode
import net.remmintan.mods.minefortress.core.FORTRESS
import net.remmintan.mods.minefortress.core.FortressGamemode
import net.remmintan.mods.minefortress.networking.c2s.ServerboundSetGamemodePacket
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper

class ProductionLineTabHandler(private val provider: IBuildingProvider) : IProductionLineTabHandler {

    override fun isCampfire(): Boolean {
        return provider.building.metadata.id == "campfire"
    }

    override fun switchToSurvivalMode() {
        // Close the current screen first
        MinecraftClient.getInstance().setScreen(null)

        // Send the gamemode change packet to the server
        val packet = ServerboundSetGamemodePacket(FortressGamemode.SURVIVAL)
        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_SET_GAMEMODE, packet)

        // Force the client to update its gamemode
        val client = MinecraftClient.getInstance()
        val interactionManager = client.interactionManager
        if (interactionManager != null && interactionManager.currentGameMode == FORTRESS) {
            // Try to switch to survival mode directly
            interactionManager.setGameMode(GameMode.SURVIVAL)
        }
    }
} 