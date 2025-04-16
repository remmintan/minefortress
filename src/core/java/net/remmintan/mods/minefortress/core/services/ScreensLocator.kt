package net.remmintan.mods.minefortress.core.services

import net.minecraft.client.gui.screen.Screen
import java.util.concurrent.ConcurrentHashMap

object ScreensLocator {

    private val registry = ConcurrentHashMap<String, () -> Screen>()

    fun register(id: String, constructor: () -> Screen) {
        registry[id] = constructor
    }

    fun get(id: String): Screen {
        return registry[id]?.invoke() ?: error("Can't find any screen of that id")
    }

}