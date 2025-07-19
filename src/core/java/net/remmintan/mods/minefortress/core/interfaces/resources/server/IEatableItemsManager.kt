package net.remmintan.mods.minefortress.core.interfaces.resources.server

import net.minecraft.util.math.BlockPos

interface IEatableItemsManager {

    fun findContainerWithFood(): BlockPos?

}