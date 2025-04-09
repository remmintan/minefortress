package org.minefortress.entity.renderer

import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.ModelWithHead
import net.minecraft.client.render.entity.model.VillagerResemblingModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import org.minefortress.entity.BasePawnEntity
import org.minefortress.entity.renderer.models.PawnModel


class VillagerHeadFeatureRenderer(
    context: FeatureRendererContext<BasePawnEntity?, PawnModel?>?,
    entityContext: EntityRendererFactory.Context
) :
    FeatureRenderer<BasePawnEntity, PawnModel?>(context) {
    // Load the villager model parts from the context
    private val villagerHeadModel =
        VillagerResemblingModel<BasePawnEntity>(entityContext.getPart(EntityModelLayers.VILLAGER))

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
        if (entity.isInvisible) {
            return
        }

        val originalModel = this.contextModel
        val villagerAsModelWithHead = villagerHeadModel as ModelWithHead // Cast for getHead()

        // Get the head part from the villager model
        val villagerHeadPart = villagerAsModelWithHead.head

        // Copy transformations from the original head
        villagerHeadPart.copyTransform(originalModel!!.head)

        // Render only the villager head part
        val vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(VILLAGER_TEXTURE))
        villagerHeadPart.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV)
    }

    companion object {
        private val VILLAGER_TEXTURE: Identifier = Identifier("textures/entity/villager/villager.png")
    }
}