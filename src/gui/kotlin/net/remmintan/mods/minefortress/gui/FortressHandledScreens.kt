package net.remmintan.mods.minefortress.gui

import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.resource.featuretoggle.FeatureFlags
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier

val BUILDING_SCREEN_HANDLER_TYPE: ScreenHandlerType<BuildingScreenHandler> = Registry.register(
    Registries.SCREEN_HANDLER,
    Identifier.of("minefortress", "building_screen_handler"),
    ScreenHandlerType(::BuildingScreenHandler, FeatureFlags.VANILLA_FEATURES)
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