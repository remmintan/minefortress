package net.remmintan.mods.minefortress.building

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.IntProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.remmintan.mods.minefortress.blueprints.isBlueprintWorld
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.BLUEPRINT_DIMENSION_KEY

val PROFESSIONS_TYPE_PROP = EnumProperty.of("profession", ProfessionType::class.java)
val CAPACITY_PROP = IntProperty.of("capacity", 0, 30)

class BuildingBlock : BlockWithEntity(FabricBlockSettings
    .create()
    .dropsNothing()
    .solidBlock { _, world, _ -> world is World && world.registryKey == BLUEPRINT_DIMENSION_KEY }
) {

    init {
        defaultState = stateManager.defaultState
            .with(PROFESSIONS_TYPE_PROP, ProfessionType.CRAFTSMAN)
            .with(CAPACITY_PROP, 10)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(PROFESSIONS_TYPE_PROP, CAPACITY_PROP)
    }

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