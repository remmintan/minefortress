package net.remmintan.mods.minefortress.gui

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.BLUEPRINT_DIMENSION_KEY

class BuildingConfigurationScreenHandler(
    syncId: Int,
    ignoredInv: Inventory?,
    private val propertyDelegate: PropertyDelegate = ArrayPropertyDelegate(2)
) : ScreenHandler(BUILDING_CONFIGURATION_SCREEN_HANDLER_TYPE, syncId) {

    init {
        addProperties(propertyDelegate)
    }

    override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack {
        // NO OP
        return ItemStack.EMPTY
    }

    override fun canUse(player: PlayerEntity?): Boolean = player?.world?.registryKey == BLUEPRINT_DIMENSION_KEY

    fun getCapacity() = if (propertyDelegate[0] <= 0) "" else propertyDelegate[0].toString()
    fun getProfession(): ProfessionType = ProfessionType.entries[propertyDelegate[1]]
}