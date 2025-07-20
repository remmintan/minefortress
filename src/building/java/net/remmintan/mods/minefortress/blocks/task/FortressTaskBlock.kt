package net.remmintan.mods.minefortress.blocks.task

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos

private val SETTINGS = FabricBlockSettings.create().strength(-1.0f, 3600000.0f).dropsNothing().noCollision()

class FortressTaskBlock : BlockWithEntity(SETTINGS) {

    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity {
        return FortressTaskBlockEntity(pos, state)
    }
}