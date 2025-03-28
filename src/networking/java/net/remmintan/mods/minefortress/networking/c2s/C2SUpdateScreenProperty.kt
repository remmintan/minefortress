package net.remmintan.mods.minefortress.networking.c2s

import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket

class C2SUpdateScreenProperty(private val index: Int, private val value: Int) : FortressC2SPacket {

    constructor(buf: PacketByteBuf) : this(buf.readInt(), buf.readInt())

    override fun write(buf: PacketByteBuf?) {
        buf?.writeInt(index)
        buf?.writeInt(value)
    }

    override fun handle(server: MinecraftServer, player: ServerPlayerEntity) {
        val handler = player.currentScreenHandler
        handler?.setProperty(index, value)
    }

    companion object {
        const val CHANNEL = "update_screen_property"
    }
}