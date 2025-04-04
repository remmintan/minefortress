package net.remmintan.mods.minefortress.networking.c2s

import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.remmintan.mods.minefortress.core.FORTRESS
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket

/**
 * Packet sent from the client to the server when the player clicks the
 * "Manage the village" button in the SurvivalCampfireScreen.
 */
class C2SSwitchToFortressModePacket() : FortressC2SPacket {

    constructor(ignoredBuf: PacketByteBuf?) : this()

    override fun handle(server: MinecraftServer, player: ServerPlayerEntity) {
        // Switch player's game mode to FORTRESS
        player.changeGameMode(FORTRESS)
        // Teleport player above the campfire
        val fortressServerManager = getFortressManager(player)
        fortressServerManager.jumpToCampfire(player)
    }

    override fun write(buf: PacketByteBuf) {
        // No data needed
    }

    companion object {
        const val CHANNEL: String = "switch_to_fortress_mode"
    }
}
