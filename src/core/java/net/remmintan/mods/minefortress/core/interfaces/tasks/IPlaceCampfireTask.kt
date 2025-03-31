package net.remmintan.mods.minefortress.core.interfaces.tasks

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

interface IPlaceCampfireTask {

    val start: BlockPos
    val end: BlockPos
    fun execute(world: ServerWorld, player: ServerPlayerEntity): BlockPos

}