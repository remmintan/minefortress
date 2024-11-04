package net.remmintan.mods.minefortress.gui

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler

class BuildingScreenHandler(syncId: Int, inventory: Inventory) : ScreenHandler(BUILDING_SCREEN_HANDLER_TYPE, syncId) {

    override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack {
        TODO("Not yet implemented")
    }

    override fun canUse(player: PlayerEntity?): Boolean {
        TODO("Check that the player is actually an owner of the fortress")
    }


}