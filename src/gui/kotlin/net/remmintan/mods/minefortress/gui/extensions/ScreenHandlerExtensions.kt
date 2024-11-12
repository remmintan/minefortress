package net.remmintan.mods.minefortress.gui.extensions

import net.minecraft.screen.ScreenHandler
import net.remmintan.mods.minefortress.networking.c2s.C2SUpdateScreenProperty


fun ScreenHandler.setProperty(index: Int, value: Int) {
    val packet = C2SUpdateScreenProperty(index, value)
}