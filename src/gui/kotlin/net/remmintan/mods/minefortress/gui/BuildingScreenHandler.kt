package net.remmintan.mods.minefortress.gui

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler

class BuildingScreenHandler(syncId: Int, _inventory: Inventory?) : ScreenHandler(BUILDING_SCREEN_HANDLER_TYPE, syncId) {

    override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack {
        TODO("Not implemented")
    }

    override fun canUse(player: PlayerEntity?): Boolean = true


}