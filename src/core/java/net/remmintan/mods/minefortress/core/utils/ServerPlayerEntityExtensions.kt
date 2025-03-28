package net.remmintan.mods.minefortress.core.utils

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.entities.player.IFortressPlayerEntity
import net.remmintan.mods.minefortress.core.interfaces.server.IPlayerManagersProvider

fun ServerPlayerEntity.getManagersProvider(): IPlayerManagersProvider {
    return this as IPlayerManagersProvider
}

fun ServerPlayerEntity.setFortressPos(pos: BlockPos) {
    (this as IFortressPlayerEntity).set_FortressPos(pos)
}