package net.remmintan.mods.minefortress.blocks.campfire

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
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

        // Add vanilla campfire properties
        val LIT: BooleanProperty = Properties.LIT // Controls if the fire is burning
        val SIGNAL_FIRE: BooleanProperty =
            Properties.SIGNAL_FIRE // If it produces a tall smoke column (hay bale underneath)
        val WATERLOGGED: BooleanProperty = Properties.WATERLOGGED // If it's waterlogged

        private val SHAPE: VoxelShape = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 7.0, 16.0)
    }

    init {
        defaultState = stateManager.defaultState
            .with(FACING, Direction.NORTH)
            .with(LIT, true) // Default to lit
            .with(SIGNAL_FIRE, false) // Default to no signal fire
            .with(WATERLOGGED, false) // Default to not waterlogged
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING, LIT, SIGNAL_FIRE, WATERLOGGED) // Add all properties
    }

    // Updated placement logic to handle waterlogging and facing
    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val worldAccess = ctx.world
        val blockPos = ctx.blockPos
        val fluidState = worldAccess.getFluidState(blockPos)
        return defaultState
            .with(FACING, ctx.horizontalPlayerFacing.opposite)
            .with(WATERLOGGED, fluidState.fluid === Fluids.WATER)
            .with(LIT, !fluidState.isIn(net.minecraft.registry.tag.FluidTags.WATER))
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return FortressCampfireBlockEntity(pos, state)
    }


    @Suppress("OVERRIDE_DEPRECATION")
    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        if (state.get(LIT)) { // Check if the campfire is lit
            if (random.nextInt(10) == 0) {
                world.playSound(
                    pos.x.toDouble() + 0.5,
                    pos.y.toDouble() + 0.5,
                    pos.z.toDouble() + 0.5,
                    SoundEvents.BLOCK_CAMPFIRE_CRACKLE,
                    SoundCategory.BLOCKS,
                    0.5f + random.nextFloat(),
                    random.nextFloat() * 0.7f + 0.6f,
                    false
                )
            }
            if (random.nextInt(5) == 0) {
                for (i in 0 until random.nextInt(1) + 1) {
                    world.addParticle(
                        ParticleTypes.LAVA, // Campfire pop particle
                        pos.x.toDouble() + 0.5,
                        pos.y.toDouble() + 0.5,
                        pos.z.toDouble() + 0.5,
                        random.nextFloat().toDouble() / 2.0,
                        5.0E-5,
                        random.nextFloat().toDouble() / 2.0
                    )
                }
            }
            // Add smoke particles - check vanilla CampfireBlock.spawnSmokeParticles for exact logic
            if (CampfireBlock.isLitCampfire(state)) {
                CampfireBlock.spawnSmokeParticle(
                    world,
                    pos,
                    state.get(SIGNAL_FIRE),
                    false
                )// Helper method or check state directly
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape {
        return SHAPE // Return the defined shape
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