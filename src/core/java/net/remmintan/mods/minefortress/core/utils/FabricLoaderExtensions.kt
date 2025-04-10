package net.remmintan.mods.minefortress.core.utils

import net.fabricmc.loader.api.FabricLoader

fun FabricLoader.getMineFortressVersion(): String {
    return getModContainer("minefortress")
        .map { it.metadata.version.friendlyString }
        .orElseThrow()
}