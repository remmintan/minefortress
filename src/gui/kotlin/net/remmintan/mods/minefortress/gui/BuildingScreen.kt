package net.remmintan.mods.minefortress.gui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class BuildingScreen(handler: BuildingScreenHandler, playerInventory: PlayerInventory, title: Text) :
    HandledScreen<BuildingScreenHandler>(handler, playerInventory, title) {



    override fun drawBackground(context: DrawContext?, delta: Float, mouseX: Int, mouseY: Int) {

    }

    companion object {
        val TEXTURE = Identifier("textures/gui/container/creative_inventory/tabs.png")
        const val TAB_TEXTURE_PREFIX: String = "textures/gui/container/creative_inventory/tab_"
    }

}