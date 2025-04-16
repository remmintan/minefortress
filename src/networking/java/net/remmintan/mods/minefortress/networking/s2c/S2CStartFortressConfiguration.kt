package net.remmintan.mods.minefortress.networking.s2c

import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket
import net.remmintan.mods.minefortress.core.services.ScreensLocator

class S2CStartFortressConfiguration() : FortressS2CPacket {

    constructor(buf: PacketByteBuf?) : this()

    companion object {
        const val CHANNEL: String = "minefortress_start_config"
    }

    override fun write(buf: PacketByteBuf?) {}

    override fun handle(client: MinecraftClient?) {
        client?.execute { client.setScreen(ScreensLocator.get("fortress configuration")) }
    }

}