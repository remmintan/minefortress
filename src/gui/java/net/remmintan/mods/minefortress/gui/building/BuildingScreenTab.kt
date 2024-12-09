package net.remmintan.mods.minefortress.gui.building

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

class BuildingScreenTab(icon: Item, column: Int, name: String, val type: BuildingScreenTabType) {
    val tabX = 27 * column
    private val tabY = -29
    val tabU = 26 * column

    val icon = ItemStack(icon)
    val name: Text = Text.literal(name)

    fun isHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= tabX && mouseX < tabX + 21 && mouseY >= tabY && mouseY < tabY + 29
    }
}