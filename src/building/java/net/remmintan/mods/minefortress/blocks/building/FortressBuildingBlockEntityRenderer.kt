package net.remmintan.mods.minefortress.blocks.building

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack


class FortressBuildingBlockEntityRenderer : BlockEntityRenderer<FortressBuildingBlockEntity> {

    override fun render(
        entity: FortressBuildingBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vcp: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        BuildingsHudRenderer.addVisibleBuilding(entity.pos)
    }
}
