package net.remmintan.mods.minefortress.core.utils

import net.minecraft.server.network.ServerPlayerEntity
import net.remmintan.mods.minefortress.core.interfaces.server.IPlayerManagersProvider

fun ServerPlayerEntity.getManagersProvider(): IPlayerManagersProvider {
    return this as IPlayerManagersProvider
}