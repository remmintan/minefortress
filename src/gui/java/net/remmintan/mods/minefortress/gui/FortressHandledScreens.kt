package net.remmintan.mods.minefortress.gui

import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.resource.featuretoggle.FeatureFlags
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier
import net.remmintan.mods.minefortress.gui.building.BuildingScreen
import net.remmintan.mods.minefortress.gui.building.BuildingScreenHandler

val BUILDING_SCREEN_HANDLER_TYPE: ScreenHandlerType<BuildingScreenHandler> = Registry.register(
    Registries.SCREEN_HANDLER,
    Identifier.of("minefortress", "building_screen_handler"),
    ScreenHandlerType(
        { syncId: Int, i: PlayerInventory -> BuildingScreenHandler(syncId) },
        FeatureFlags.VANILLA_FEATURES
    )
)

val BUILDING_CONFIGURATION_SCREEN_HANDLER_TYPE: ScreenHandlerType<BuildingConfigurationScreenHandler> =
    Registry.register(
        Registries.SCREEN_HANDLER,
        Identifier.of("minefortress", "building_configuration_screen_handler"),
        ScreenHandlerType(::BuildingConfigurationScreenHandler, FeatureFlags.VANILLA_FEATURES)
    )

fun registerScreens() {
    HandledScreens.register(BUILDING_SCREEN_HANDLER_TYPE, ::BuildingScreen)
    HandledScreens.register(BUILDING_CONFIGURATION_SCREEN_HANDLER_TYPE, ::BuildingConfigurationScreen)
}