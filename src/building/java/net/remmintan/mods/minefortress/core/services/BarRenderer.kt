package net.remmintan.mods.minefortress.core.services

import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier
import net.remmintan.mods.minefortress.core.dtos.buildings.BarColor

private val BARS_TEXTURE = Identifier("minefortress", "textures/gui/bars.png")
private const val TEXTURE_SIDE = 256

class BarRenderer(
    private val ctx: DrawContext,
    private val barWidth: Float = 182f,
    private val barOverlayTextureNumber: Int = 22,
    private val texture: Identifier = BARS_TEXTURE
) {


    fun renderBarWithProgress(barNumber: Int, barColor: BarColor, progress: Float = 1.0f) {
        renderSingleBar(barNumber, barColor.barTextureNumber)
        renderSingleBar(barNumber, barOverlayTextureNumber)
        renderSingleBar(barNumber, barColor.barTextureNumber + 1, progress)
    }

    private fun renderSingleBar(barNumber: Int, barTextureNumber: Int, progress: Float = 1.0f) {
        val barHeight = 5
        val finalWidth = (barWidth * progress).toInt()

        val barX = (-barWidth / 2f).toInt()
        val barY = (barNumber * (barHeight * 1.3f)).toInt()

        val v = 5f

        val barV = barTextureNumber * v

        ctx.drawTexture(texture, barX, barY, 0f, barV, finalWidth, barHeight, TEXTURE_SIDE, TEXTURE_SIDE)
    }

}