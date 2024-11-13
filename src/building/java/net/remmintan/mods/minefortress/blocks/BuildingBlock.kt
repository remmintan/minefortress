package net.remmintan.mods.minefortress.blocks

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.remmintan.mods.minefortress.blueprints.isBlueprintWorld
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.BLUEPRINT_DIMENSION_KEY

class BuildingBlock : BlockWithEntity(FabricBlockSettings
    .create()
    .dropsNothing()
    .solidBlock { _, world, _ -> world is World && world.registryKey == BLUEPRINT_DIMENSION_KEY }
) {

    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity {
        return BuildingBlockEntity(pos, state)
    }

    @Deprecated("Deprecated in Java")
    override fun onUse(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        val ent = world?.getBlockEntity(pos)
        if (world?.isBlueprintWorld() == true && ent is NamedScreenHandlerFactory) {
            player?.openHandledScreen(ent)
            return ActionResult.SUCCESS
        }

        return ActionResult.PASS
    }

    @Deprecated("Deprecated in Java")
    override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.MODEL


}