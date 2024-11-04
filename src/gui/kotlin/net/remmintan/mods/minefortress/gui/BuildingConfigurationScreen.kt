package net.remmintan.mods.minefortress.gui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class BuildingConfigurationScreen(
    handler: BuildingConfigurationScreenHandler,
    playerInventory: PlayerInventory,
    title: Text
) :
    HandledScreen<BuildingConfigurationScreenHandler>(handler, playerInventory, title) {

    override fun drawBackground(context: DrawContext?, delta: Float, mouseX: Int, mouseY: Int) {
    }
}