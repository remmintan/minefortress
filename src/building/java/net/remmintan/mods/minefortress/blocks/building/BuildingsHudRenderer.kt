package net.remmintan.mods.minefortress.blocks.building

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

private val BARS_TEXTURE = Identifier("minefortress", "textures/gui/bars.png")
private const val TEXTURE_SIDE = 256

object BuildingsHudRenderer {

    private val visibleBuildings = mutableSetOf<BlockPos>()

    fun addVisibleBuilding(pos: BlockPos) {
        visibleBuildings.add(pos)
    }

    fun register() {
        HudRenderCallback.EVENT.register { context, tickDelta ->
            TODO()

//            for (buildingPos in visibleBuildings) {
//                val rel = buildingPos.toCenterPos().subtract(camera.pos)
//                val vec = Vector4f(rel.x.toFloat(), rel.y.toFloat(), rel.z.toFloat(), 1.0f);
//                val projectedVec = vec.mul(modelView).mul(proj)
//
//                if (projectedVec.w > 0) {
//                    val xNdc = projectedVec.x / projectedVec.w
//                    val yNdc = projectedVec.y / projectedVec.w
//
//                    val screenX = (xNdc + 1f) / 2f * screenW
//                    val screenY = (1f-yNdc) / 2f * screenH
//
//                    val matrices = context.matrices
//                    matrices.push()
//                    matrices.translate(screenX.toDouble(), screenY.toDouble(), 0.0)
//
//                    val barRenderer = BarRenderer(ctx = context)
//                    barRenderer.barWidth = 182f
//                    barRenderer.renderBarWithProgress(0, BarColor.BLUE, 0.33f)
//
//                    matrices.pop()
//                }
//            }
        }
    }

    private class BarRenderer(
        private val ctx: DrawContext,
    ) {

        var barWidth: Float = 10f

        fun renderBarWithProgress(barNumber: Int, barColor: BarColor, progress: Float = 1.0f) {
            renderSingleBar(barNumber, barColor.barTextureNumber)
            renderSingleBar(barNumber, barColor.barTextureNumber + 1, progress)
            renderSingleBar(barNumber, 22)
        }

        private fun renderSingleBar(barNumber: Int, barTextureNumber: Int, progress: Float = 1.0f) {
            val barHeight = 5
            val finalWidth = (barWidth * progress).toInt()

            val barX = (-barWidth / 2f).toInt()
            val barY = barNumber * barHeight

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