package net.remmintan.mods.minefortress.networking.c2s

import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket

class C2SOpenBuildingScreen(private val pos: BlockPos) : FortressC2SPacket {

    constructor(buf: PacketByteBuf) : this(buf.readBlockPos())

    override fun handle(server: MinecraftServer?, player: ServerPlayerEntity?) {
        player?.world?.getBlockEntity(pos).let {
            if (it is NamedScreenHandlerFactory) {
                player?.openHandledScreen(it)
            }
        }

    }

    override fun write(buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
    }

    companion object {
        const val CHANNEL = "open_building_screen"
    }
}