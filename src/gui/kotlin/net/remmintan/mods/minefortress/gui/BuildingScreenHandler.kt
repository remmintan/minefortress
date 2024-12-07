package net.remmintan.mods.minefortress.gui

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler

class BuildingScreenHandler(
    syncId: Int,
    propertyDelegate: PropertyDelegate = ArrayPropertyDelegate(3)
) : ScreenHandler(BUILDING_SCREEN_HANDLER_TYPE, syncId) {

    init {
        addProperties(propertyDelegate)
    }

    override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun canUse(player: PlayerEntity?): Boolean = true


}