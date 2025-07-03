package net.remmintan.mods.minefortress.networking.c2s

import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket

/**
 * This packet is used to switch the player from Fortress mode to Minecraft's Survival mode
 * without requiring cheats to be enabled in the world.
 */
class C2SSwitchToMinecraftSurvival : FortressC2SPacket {

    constructor()
    constructor(ignoredBuf: PacketByteBuf?)

    override fun handle(server: MinecraftServer, player: ServerPlayerEntity) {
        // First teleport the player to the ground near the campfire
        val fortressServerManager = getFortressManager(player)
        fortressServerManager.teleportToCampfireGround(player)

        // Then change the player's game mode to Survival directly on the server
        player.inventory.clear()
        player.changeGameMode(GameMode.SURVIVAL)
    }

    override fun write(buf: PacketByteBuf) {
        // No data to write
    }

    companion object {
        const val CHANNEL: String = "switch_to_minecraft_survival"
    }
} 