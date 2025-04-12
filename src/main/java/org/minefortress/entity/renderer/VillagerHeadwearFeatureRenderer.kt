package org.minefortress.entity.renderer

import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.util.math.MatrixStack
import org.minefortress.entity.BasePawnEntity
import org.minefortress.entity.renderer.models.PawnModel

// Still need the context type
// Note: We extend FeatureRenderer with PawnModel because that's what PawnRenderer uses
class VillagerHeadwearFeatureRenderer(
    context: FeatureRendererContext<BasePawnEntity?, PawnModel?>?,
    entityContext: EntityRendererFactory.Context
) :
    FeatureRenderer<BasePawnEntity, PawnModel?>(context) {
    private val villagerHeadwearModel =
        PlayerEntityModel<BasePawnEntity>(entityContext.getPart(EntityModelLayers.PLAYER), false)

    override fun render(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        entity: BasePawnEntity,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float
    ) {
        if (entity.isInvisible || entity.isSleeping) { // Also don't render headwear if sleeping
            return
        }
        matrices.push()
        matrices.scale(1.01f, 1.01f, 1.01f)
        matrices.translate(0.0, -0.12, 0.0)

        val originalModel = this.contextModel
        val villagerHeadPart = villagerHeadwearModel.head
        val villagerHatPart = villagerHeadwearModel.hat

        // Copy transformations from the original head to ensure alignment
        villagerHeadPart.copyTransform(originalModel!!.head)
        villagerHatPart.copyTransform(originalModel.hat)

        // Get the correct clothing texture based on profession
        val clothingTexture = PawnClothesFeature.getClothesTexture(entity.clothingId) // Reuse logic or make static

        // Render only the villager head part using the clothing texture
        // Use a translucent layer to overlay the clothes
        val vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(clothingTexture))
        villagerHeadPart.render(
            matrices,
            vertexConsumer,
            light,
            OverlayTexture.DEFAULT_UV,
            1.0f,
            1.0f,
            1.0f,
            1.0f
        )
        villagerHatPart.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV)

        matrices.pop()
    }
}