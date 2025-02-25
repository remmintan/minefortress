package net.remmintan.mods.minefortress.gui.building

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.IScreenHandlerWithTabs
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding
import net.remmintan.mods.minefortress.core.isFortressGamemode
import net.remmintan.mods.minefortress.gui.BUILDING_SCREEN_HANDLER_TYPE
import net.remmintan.mods.minefortress.gui.building.handlers.*


class BuildingScreenHandler(
    syncId: Int,
    private val propertyDelegate: PropertyDelegate = ArrayPropertyDelegate(4)
) : ScreenHandler(BUILDING_SCREEN_HANDLER_TYPE, syncId),
    IInfoTabHandler by InfoTabHandler(getBuildingProvider(propertyDelegate)),
    IWorkforceTabHandler by WorkforceTabHandler(getBuildingProvider(propertyDelegate)),
    IProductionLineTabHandler by ProductionLineTabHandler(getBuildingProvider(propertyDelegate)),
    IScreenHandlerWithTabs {

    val tabs: List<BuildingScreenTab> by lazy {
        if (this.getProfessions().isEmpty()) {
            listOf(
                BuildingScreenTab(Items.COBBLESTONE, 0, "Info", BuildingScreenTabType.INFO),
                BuildingScreenTab(Items.DIAMOND, 1, "Production Line", BuildingScreenTabType.PRODUCTION_LINE),
            )
        } else {
            listOf(
                BuildingScreenTab(Items.COBBLESTONE, 0, "Info", BuildingScreenTabType.INFO),
                BuildingScreenTab(Items.PLAYER_HEAD, 1, "Workforce", BuildingScreenTabType.WORKFORCE),
                BuildingScreenTab(Items.DIAMOND, 2, "Production Line", BuildingScreenTabType.PRODUCTION_LINE),
            )
        }
    }

    override var selectedTabIndex: Int
        get() {
            return propertyDelegate[3]
        }
        set(value) {
            propertyDelegate[3] = value
        }

    var selectedTab: BuildingScreenTab
        get() {
            return tabs[selectedTabIndex]
        }
        set(value) {
            selectedTabIndex = tabs.indexOf(value)
        }

    init {
        addProperties(propertyDelegate)
    }

    override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun canUse(player: PlayerEntity?): Boolean = isFortressGamemode(player)

}

private fun getBuildingProvider(propertyDelegate: PropertyDelegate): IBuildingProvider {
    return object : IBuildingProvider {
        override val building: IFortressBuilding by lazy {
            getBuildingFromPropertyDelegate(propertyDelegate)
        }
    }
}

private fun getBuildingFromPropertyDelegate(propertyDelegate: PropertyDelegate): IFortressBuilding {
    val world = MinecraftClient.getInstance().world ?: error("Can't access the world")

    val x = propertyDelegate[0]
    val y = propertyDelegate[1]
    val z = propertyDelegate[2]

    val blockPos = BlockPos(x, y, z)
    return world.getBlockEntity(blockPos) as? IFortressBuilding
        ?: error("Can't access the building from the screen handler")
}