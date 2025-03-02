package net.remmintan.mods.minefortress.gui.building.functions

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.remmintan.mods.minefortress.gui.building.translateMousePosition
import net.remmintan.mods.minefortress.networking.c2s.C2SSwitchToMinecraftSurvival
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper

/**
 * Implementation of building functions for the Campfire building
 */
class CampfireFunctions : IBuildingFunctions {

    private var switchToSurvivalButton: ButtonWidget? = null
    override fun render(context: DrawContext, x: Int, y: Int, width: Int, height: Int, mouseX: Int, mouseY: Int) {

        // Create the button if it doesn't exist
        if (switchToSurvivalButton == null) {
            switchToSurvivalButton = ButtonWidget.builder(
                Text.literal("Explore your village!"),
                { switchToSurvivalMode() }
            )
                .dimensions(0, 0, 200, 20)
                .build()
        }

        // Update button position
        switchToSurvivalButton!!.setPosition(x + width / 2 - 100, y + 40)

        // Render the button
        val (translatedMouseX, translatedMouseY) = context.matrices.translateMousePosition(mouseX, mouseY)
        switchToSurvivalButton!!.render(context, translatedMouseX, translatedMouseY, 0f)
    }

    override fun onMouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (switchToSurvivalButton != null && switchToSurvivalButton!!.isHovered) {
            switchToSurvivalButton!!.onClick(mouseX, mouseY)
            return true
        }
        return false
    }

    override fun tick() {
        // No tick logic needed for now
    }

    private fun switchToSurvivalMode() {
        // Close the current screen first
        MinecraftClient.getInstance().setScreen(null)

        // Then, send a packet to switch to actual Minecraft Survival mode
        // This works without requiring cheats to be enabled
        val survivalPacket = C2SSwitchToMinecraftSurvival()
        FortressClientNetworkHelper.send(C2SSwitchToMinecraftSurvival.CHANNEL, survivalPacket)
    }
} 