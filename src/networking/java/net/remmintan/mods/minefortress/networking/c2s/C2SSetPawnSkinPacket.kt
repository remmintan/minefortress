package net.remmintan.mods.minefortress.networking.c2s

import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.remmintan.mods.minefortress.core.dtos.PawnSkin
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket

class C2SSetPawnSkinPacket(private val skin: PawnSkin) : FortressC2SPacket {

    constructor(buf: PacketByteBuf) : this(PawnSkin.valueOf(buf.readString()))

    override fun handle(server: MinecraftServer, player: ServerPlayerEntity) {
        getFortressManager(player).setPawnsSkin(skin)
    }

    override fun write(buf: PacketByteBuf?) {
        buf?.writeString(skin.name)
    }

    companion object {
        const val CHANNEL = "set_pawn_skin"
    }
}