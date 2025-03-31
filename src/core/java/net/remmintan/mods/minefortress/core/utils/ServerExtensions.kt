package net.remmintan.mods.minefortress.core.utils

import net.minecraft.screen.ScreenHandler
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.FortressGamemode
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressHolder
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider

fun MinecraftServer.isSurvivalFortress(): Boolean {
    return (this as IFortressServer)._FortressGamemode == FortressGamemode.SURVIVAL
}

fun MinecraftServer.isCreativeFortress(): Boolean {
    return (this as IFortressServer)._FortressGamemode == FortressGamemode.CREATIVE
}

fun MinecraftServer.sendMessageToFortressOwner(fortressPos: BlockPos, message: String) {
    val fortressHolder = getFortressHolder(fortressPos)
    fortressHolder?.getFortressOwner()?.sendMessage(Text.of(message))
}

private fun MinecraftServer.getFortressHolder(fortressPos: BlockPos) =
    this.overworld.getBlockEntity(fortressPos) as? IFortressHolder

fun MinecraftServer.fortressOwnerHasScreenOpened(
    fortressPos: BlockPos,
    screenHandlerClass: Class<out ScreenHandler>
): Boolean {
    return this.getFortressHolder(fortressPos)
        ?.getFortressOwner()
        ?.currentScreenHandler
        ?.let { screenHandlerClass.isInstance(it) } ?: false
}

fun MinecraftServer.getFortressOwner(fortressPos: BlockPos): ServerPlayerEntity? {
    return this.getFortressHolder(fortressPos)?.getFortressOwner()
}

fun MinecraftServer.getManagersProvider(fortressPos: BlockPos): IServerManagersProvider? {
    return this.getFortressHolder(fortressPos)?.getServerManagersProvider()
}

fun MinecraftServer.getFortressManager(fortressPos: BlockPos): IServerFortressManager? {
    return this.getFortressHolder(fortressPos)?.getServerFortressManager()
}