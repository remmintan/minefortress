package net.remmintan.mods.minefortress.core.utils

import net.minecraft.client.MinecraftClient
import net.remmintan.mods.minefortress.core.FortressGamemode
import net.remmintan.mods.minefortress.core.interfaces.IFortressGamemodeHolder

fun MinecraftClient.isSurvivalFortress(): Boolean {
    return (this as IFortressGamemodeHolder)._fortressGamemode == FortressGamemode.SURVIVAL
}

fun MinecraftClient.isCreativeFortress(): Boolean {
    return (this as IFortressGamemodeHolder)._fortressGamemode == FortressGamemode.CREATIVE
}