package net.remmintan.mods.minefortress.blocks.building

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f

private val BARS_TEXTURE = Identifier("minefortress", "textures/gui/bars.png")
private const val TEXTURE_SIDE = 256

object BuildingsHudRenderer {

    private val visibleBuildings = mutableSetOf<BuildingRenderState>()

    fun addVisibleBuilding(pos: Vec2f, distance: Double) {
        val state = BuildingRenderState(pos, distance.toFloat())
        visibleBuildings.add(state)
    }

    fun register() {
        HudRenderCallback.EVENT.register { context, tickDelta ->
            for ((screenPos, distance) in visibleBuildings) {
                val matrices = context.matrices
                matrices.push()
                matrices.translate(screenPos.x / 2.0, screenPos.y / 2.0, 0.0)

                val adjustedDistance = distance / 5f
                matrices.scale(1 / adjustedDistance, 1 / adjustedDistance, 1 / adjustedDistance)

                val barRenderer = BarRenderer(ctx = context)
                barRenderer.renderBarWithProgress(0, BarColor.GREEN, 1f)
                barRenderer.renderBarWithProgress(1, BarColor.BLUE, 0.45f)

                matrices.pop()
            }

            visibleBuildings.clear()
        }
    }

    private class BarRenderer(
        private val ctx: DrawContext,
    ) {

        var barWidth: Float = 182f

        fun renderBarWithProgress(barNumber: Int, barColor: BarColor, progress: Float = 1.0f) {
            renderSingleBar(barNumber, barColor.barTextureNumber)
            renderSingleBar(barNumber, barColor.barTextureNumber + 1, progress)
            renderSingleBar(barNumber, 22)
        }

        private fun renderSingleBar(barNumber: Int, barTextureNumber: Int, progress: Float = 1.0f) {
            val barHeight = 5
            val finalWidth = (barWidth * progress).toInt()

            val barX = (-barWidth / 2f).toInt()
            val barY = (barNumber * (barHeight * 1.3f)).toInt()

            val v = 5f

            val barV = barTextureNumber * v

            ctx.drawTexture(BARS_TEXTURE, barX, barY, 0f, barV, finalWidth, barHeight, TEXTURE_SIDE, TEXTURE_SIDE)
        }

    }

    private enum class BarColor(val barTextureNumber: Int) {
        PINK(0),
        BLUE(2),
        RED(4),
        GREEN(6),
        YELLOW(8),
        PURPLE(10),
        GRAY(12)
    }

}