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
        val tabsList = mutableListOf<BuildingScreenTab>()

        // Always add the Info tab
        tabsList.add(BuildingScreenTab(Items.COBBLESTONE, 0, "Info", BuildingScreenTabType.INFO))

        // Add Workforce tab if there are professions
        if (this.getProfessions().isNotEmpty()) {
            tabsList.add(
                BuildingScreenTab(
                    Items.PLAYER_HEAD,
                    tabsList.size,
                    "Workforce",
                    BuildingScreenTabType.WORKFORCE
                )
            )
        }

        // Add Production Line tab only for Campfire
        if (this.isCampfire()) {
            tabsList.add(
                BuildingScreenTab(
                    Items.DIAMOND,
                    tabsList.size,
                    "Functions",
                    BuildingScreenTabType.PRODUCTION_LINE
                )
            )
        }

        tabsList
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
            val index = selectedTabIndex.coerceIn(0, tabs.size - 1)
            return tabs[index]
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