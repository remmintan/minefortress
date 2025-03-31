package net.remmintan.mods.minefortress.blocks.campfire

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.remmintan.mods.minefortress.blocks.FortressBlocks

class FortressCampfireBlock : BlockWithEntity(
    FabricBlockSettings.create()
        .strength(2.0f)
        .nonOpaque()
        .luminance(15)
) {

    companion object {
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
    }

    init {
        defaultState = stateManager.defaultState
            .with(FACING, Direction.NORTH)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        return defaultState.with(FACING, ctx.horizontalPlayerFacing.opposite)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return FortressCampfireBlockEntity(pos, state)
    }


    @Suppress("OVERRIDE_DEPRECATION")
    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return if (type == FortressBlocks.CAMPFIRE_ENT_TYPE) {
            BlockEntityTicker { w, p, s, be -> if (be is FortressCampfireBlockEntity) be.tick(w, p, s) }
        } else {
            null
        }
    }
} 