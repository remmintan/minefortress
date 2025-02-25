package net.remmintan.mods.minefortress.gui.building.handlers

import net.minecraft.client.MinecraftClient
import net.remmintan.mods.minefortress.core.FortressGamemode
import net.remmintan.mods.minefortress.networking.c2s.C2SSwitchToMinecraftSurvival
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

        // First, switch the Fortress gamemode to SURVIVAL
        val fortressPacket = ServerboundSetGamemodePacket(FortressGamemode.SURVIVAL)
        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_SET_GAMEMODE, fortressPacket)

        // Then, send a packet to switch to actual Minecraft Survival mode
        // This works without requiring cheats to be enabled
        val survivalPacket = C2SSwitchToMinecraftSurvival()
        FortressClientNetworkHelper.send(C2SSwitchToMinecraftSurvival.CHANNEL, survivalPacket)
    }
} 