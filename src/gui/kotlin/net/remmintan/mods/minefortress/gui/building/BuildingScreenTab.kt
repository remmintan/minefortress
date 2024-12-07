package net.remmintan.mods.minefortress.gui.building

import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class BuildingScreenTab(icon: Item, val column: Int) {
    val tabX = 27 * column
    val icon = ItemStack(icon)
}