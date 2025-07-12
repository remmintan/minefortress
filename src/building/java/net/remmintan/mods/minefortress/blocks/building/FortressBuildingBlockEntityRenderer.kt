package net.remmintan.mods.minefortress.blocks.building

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.Items
import net.remmintan.mods.minefortress.core.utils.camera.CameraTools


class FortressBuildingBlockEntityRenderer : BlockEntityRenderer<FortressBuildingBlockEntity> {

    override fun render(
        entity: FortressBuildingBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vcp: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val minecraft = MinecraftClient.getInstance()
        val buildingBlockCenter = entity.pos.down().toCenterPos()
        val projectedBuildingPos =
            CameraTools.projectToScreenSpace(buildingBlockCenter, minecraft)
        val distance = minecraft.cameraEntity?.pos?.distanceTo(buildingBlockCenter) ?: 1.0

        val bars = entity.bars
        val buildingMetadata = entity.metadata
        val icon = if (buildingMetadata.id == "campfire")
            Items.CAMPFIRE
        else
            buildingMetadata.requirement.type?.icon
        BuildingsHudRenderer.addVisibleBuilding(projectedBuildingPos, distance, bars, icon)
    }
}
