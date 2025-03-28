package net.remmintan.mods.minefortress.core.interfaces.tasks

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.buildings.IServerBuildingsManager

interface IInstantTask {

    val start: BlockPos
    val end: BlockPos
    fun execute(world: ServerWorld, player: ServerPlayerEntity, getBuildingsManager: IServerBuildingsManager)
    fun addFinishListener(listener: () -> Unit)

}