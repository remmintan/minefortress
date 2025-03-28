package org.minefortress.fortress

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.event.GameEvent
import net.remmintan.mods.minefortress.blocks.FortressBlocks
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressCenterSetupManager

class FortressCenterSetupManager(private val player: ServerPlayerEntity) : IFortressCenterSetupManager {

    override fun setupCenter(world: ServerWorld, center: BlockPos) {
        val fortressCampfire = FortressBlocks.FORTRESS_CAMPFIRE.defaultState
        world.setBlockState(center, fortressCampfire, 3)
        world.emitGameEvent(player, GameEvent.BLOCK_PLACE, center)
        world.markDirty(center)
    }
}