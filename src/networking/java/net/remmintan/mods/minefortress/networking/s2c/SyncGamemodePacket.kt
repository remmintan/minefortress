package net.remmintan.mods.minefortress.networking.s2c

import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.remmintan.mods.minefortress.core.FortressGamemode
import net.remmintan.mods.minefortress.core.interfaces.IFortressGamemodeHolder
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket


class S2CSyncGamemodePacket(val gamemode: FortressGamemode) : FortressS2CPacket {

    constructor(buf: PacketByteBuf) : this(buf.readEnumConstant(FortressGamemode::class.java))

    override fun write(buf: PacketByteBuf) {
        buf.writeEnumConstant(gamemode)
    }

    override fun handle(client: MinecraftClient) {
        (client as IFortressGamemodeHolder)._fortressGamemode = gamemode
    }

    companion object {
        const val CHANNEL: String = "minefortress_gamemode_sync"
    }
}