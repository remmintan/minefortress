package net.remmintan.mods.minefortress.networking.c2s

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.remmintan.mods.minefortress.core.interfaces.entities.player.IFortressServerPlayerEntity
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket
import net.remmintan.mods.minefortress.core.utils.getMineFortressVersion

class C2SClientReadyPacket(private val clientVersion: String) : FortressC2SPacket {

    companion object {
        const val CHANNEL = "client_ready"
    }

    constructor(buf: PacketByteBuf) : this(buf.readString())

    override fun write(buf: PacketByteBuf?) {
        buf?.writeString(clientVersion)
    }

    override fun handle(server: MinecraftServer, player: ServerPlayerEntity) {
        val serverVersion = FabricLoader.getInstance().getMineFortressVersion()

        if (serverVersion != clientVersion) {
            val disconnectMessage =
                "MineFortress mod version is wrong! The expected version is $serverVersion but the client version is $clientVersion."
            player.networkHandler.disconnect(Text.of(disconnectMessage))
        } else {
            (player as IFortressServerPlayerEntity).set_ModVersionValidated(true)
        }
    }


}