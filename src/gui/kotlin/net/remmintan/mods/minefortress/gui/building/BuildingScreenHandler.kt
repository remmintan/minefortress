package net.remmintan.mods.minefortress.gui.building

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.remmintan.mods.minefortress.core.isClientInFortressGamemode
import net.remmintan.mods.minefortress.gui.BUILDING_SCREEN_HANDLER_TYPE

class BuildingScreenHandler(
    syncId: Int,
    propertyDelegate: PropertyDelegate = ArrayPropertyDelegate(3)
) : ScreenHandler(BUILDING_SCREEN_HANDLER_TYPE, syncId) {

    val tabs = listOf(
        BuildingScreenTab(Items.COBBLESTONE, 0),
        BuildingScreenTab(Items.PLAYER_HEAD, 1),
        BuildingScreenTab(Items.DIAMOND, 2),
    )
    var selectedTab = tabs[0]

    init {
        addProperties(propertyDelegate)
    }

    override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun canUse(player: PlayerEntity?): Boolean = isClientInFortressGamemode()

}