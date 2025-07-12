package net.remmintan.mods.minefortress.core.dtos.buildings

data class BuildingBar(val index: Int, val progress: Float, val color: BarColor)

enum class BarColor(val barTextureNumber: Int) {
    PINK(0),
    BLUE(2),
    RED(4),
    GREEN(6),
    YELLOW(8),
    PURPLE(10),
    GRAY(12)
}