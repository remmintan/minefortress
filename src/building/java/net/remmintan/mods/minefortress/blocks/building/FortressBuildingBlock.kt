package net.remmintan.mods.minefortress.blocks.building

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.remmintan.mods.minefortress.blocks.FortressBlocks

class FortressBuildingBlock : BlockWithEntity(FabricBlockSettings.create().dropsNothing()) {
    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity {
        return FortressBuildingBlockEntity(pos, state)
    }

    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return if (type == FortressBlocks.BUILDING_ENT_TYPE) {
            BlockEntityTicker { w, p, s, be -> if (be is FortressBuildingBlockEntity) be.tick(w) }
        } else {
            null
        }
    }
}