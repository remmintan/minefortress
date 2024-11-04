package net.remmintan.mods.minefortress.gui

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.IBlueprintsWorld

class BuildingConfigurationScreenHandler(
    syncId: Int,
    inventory: Inventory?,
    private val propertyDelegate: PropertyDelegate = ArrayPropertyDelegate(2)
) : ScreenHandler(BUILDING_CONFIGURATION_SCREEN_HANDLER_TYPE, syncId) {

    init {
        addProperties(propertyDelegate)
    }

    override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack {
        TODO("Not yet implemented")
    }

    override fun canUse(player: PlayerEntity?): Boolean = player?.world is IBlueprintsWorld

    fun getCapacity() = propertyDelegate[0]
    fun setCapacity(value: Int) = propertyDelegate.set(0, value)

    fun getProfession(): ProfessionType = ProfessionType.entries[propertyDelegate[1]]
    fun setProfession(value: ProfessionType) = propertyDelegate.set(1, value.ordinal)
}