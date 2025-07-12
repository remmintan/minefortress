package net.remmintan.mods.minefortress.blocks.building

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f
import net.remmintan.mods.minefortress.core.dtos.buildings.BarColor
import net.remmintan.mods.minefortress.core.dtos.buildings.BuildingBar
import net.remmintan.mods.minefortress.core.isClientInFortressGamemode

private val BARS_TEXTURE = Identifier("minefortress", "textures/gui/bars.png")
private const val TEXTURE_SIDE = 256

object BuildingsHudRenderer {

    private val visibleBuildings = mutableSetOf<BuildingRenderState>()

    fun addVisibleBuilding(pos: Vec2f, distance: Double, bars: List<BuildingBar>, icon: Item?) {
        val state = BuildingRenderState(pos, distance.toFloat(), bars, icon)
        visibleBuildings.add(state)
    }

    fun register() {
        HudRenderCallback.EVENT.register { context, tickDelta ->
            if (isClientInFortressGamemode()) {
                val scaleFactor = MinecraftClient.getInstance().window.scaleFactor
                val windowScaleRatio = scaleFactor / 2.0

                for ((screenPos, distance, bars, icon) in visibleBuildings) {
                    val matrices = context.matrices
                    matrices.push()
                    matrices.translate(
                        screenPos.x.toDouble() / windowScaleRatio,
                        screenPos.y.toDouble() / windowScaleRatio,
                        -1000.0
                    )

                    val adjustedDistance = distance / 8f
                    val ratio = 1 / adjustedDistance
                    matrices.scale(ratio, ratio, ratio)

                    val barRenderer = BarRenderer(ctx = context)
                    bars.forEach { (index, progress, color) ->
                        barRenderer.renderBarWithProgress(index, color, progress)
                    }
                    context.drawItem(ItemStack(icon), -8, -20)
                    matrices.pop()
                }
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
            renderSingleBar(barNumber, 22)
            renderSingleBar(barNumber, barColor.barTextureNumber + 1, progress)

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
}

private data class BuildingRenderState(
    val screenPos: Vec2f,
    val distance: Float,
    val bars: List<BuildingBar>,
    val icon: Item?
)