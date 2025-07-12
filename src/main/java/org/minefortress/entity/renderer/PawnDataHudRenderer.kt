package org.minefortress.entity.renderer

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f
import net.remmintan.mods.minefortress.core.dtos.buildings.HudBar
import net.remmintan.mods.minefortress.core.isClientInFortressGamemode
import net.remmintan.mods.minefortress.core.services.BarRenderer

private val BARS_SHORT_TEXTURE = Identifier("minefortress", "textures/gui/bars_short.png")

object PawnDataHudRenderer {

    private val pawnsToRender = mutableListOf<PawnRenderData>()

    fun addPawnData(pos: Vec2f, distance: Double, bars: List<HudBar>, profession: ItemStack?) {
        pawnsToRender.add(
            PawnRenderData(
                pos,
                distance,
                bars,
                profession
            )
        )
    }

    fun register() {
        HudRenderCallback.EVENT.register { context, tickDelta ->
            if (isClientInFortressGamemode()) {
                val scaleFactor = MinecraftClient.getInstance().window.scaleFactor
                val windowScaleRatio = scaleFactor / 2.0

                for ((screenPos, distance, bars, icon) in pawnsToRender) {

                    val matrices = context.matrices
                    matrices.push()
                    matrices.translate(
                        screenPos.x.toDouble() / windowScaleRatio,
                        screenPos.y.toDouble() / windowScaleRatio,
                        -1000.0
                    )

                    val adjustedDistance = distance / 8f
                    val ratio = 1f / adjustedDistance.toFloat()
                    matrices.scale(ratio, ratio, ratio)

                    val barRenderer = BarRenderer(
                        ctx = context,
                        barWidth = 27f,
                        barOverlayTextureNumber = 14,
                        texture = BARS_SHORT_TEXTURE
                    )
                    bars.forEach { (index, progress, color) ->
                        barRenderer.renderBarWithProgress(index, color, progress)
                    }
                    icon?.let {
                        context.drawItem(it, -8, -20)
                    }
                    matrices.pop()
                }
            }


            pawnsToRender.clear()
        }
    }

    private data class PawnRenderData(
        val pos: Vec2f,
        val distance: Double,
        val bars: List<HudBar>,
        val icon: ItemStack?
    )

}

