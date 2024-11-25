package net.remmintan.mods.minefortress.blocks

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos

class FortressBuildingBlock : BlockWithEntity(FabricBlockSettings.create().dropsNothing()) {
    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity {
        return FortressBuildingBlockEntity(pos, state)
    }
}