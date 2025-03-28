package net.remmintan.mods.minefortress.core.interfaces.server

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

interface IFortressCenterSetupManager {

    fun setupCenter(world: ServerWorld, center: BlockPos)

}