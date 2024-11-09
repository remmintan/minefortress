package net.remmintan.mods.minefortress.networking.c2s

import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.remmintan.mods.minefortress.blueprints.clearBlueprint
import net.remmintan.mods.minefortress.blueprints.getBlueprintWorld
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket

class C2SClearActiveBlueprint : FortressC2SPacket {
    constructor()
    constructor(ignoredBuf: PacketByteBuf?)

    override fun handle(server: MinecraftServer, player: ServerPlayerEntity) {
        server.getBlueprintWorld().clearBlueprint(player)
    }

    override fun write(buf: PacketByteBuf) {
    }

    companion object {
        const val CHANNEL: String = "clear_active_blueprint"
    }
}
