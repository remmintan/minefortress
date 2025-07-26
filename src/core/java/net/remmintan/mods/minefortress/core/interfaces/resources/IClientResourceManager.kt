package net.remmintan.mods.minefortress.core.interfaces.resources

import net.minecraft.util.math.BlockPos


interface IClientResourceManager : IResourceManager {

    fun sync(containerPositions: List<BlockPos>)

}
