package org.minefortress.registries.events

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents.AfterPlayerChange
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.GameMode
import net.remmintan.mods.minefortress.core.FORTRESS
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.BLUEPRINT_DIMENSION_KEY
import net.remmintan.mods.minefortress.core.interfaces.entities.player.FortressServerPlayerEntity

object BlueprintWorldEvents {

    fun register() {
        // initialising the fortress server on join
        ServerPlayConnectionEvents.JOIN.register(ServerPlayConnectionEvents.Join { handler: ServerPlayNetworkHandler, sender: PacketSender?, server: MinecraftServer ->
            val player = handler.player
            if (player is FortressServerPlayerEntity) {
                if (player.was_InBlueprintWorldWhenLoggedOut() && player._PersistedPos != null) {
                    val pos = player._PersistedPos
                    player.teleport(pos!!.x, pos.y, pos.z)
                }
            }
        })

        ServerPlayConnectionEvents.DISCONNECT.register(ServerPlayConnectionEvents.Disconnect { handler: ServerPlayNetworkHandler, server: MinecraftServer? ->
            val player = handler.player
            val inBlueprintWorldOnDisconnect = player.world.registryKey === BLUEPRINT_DIMENSION_KEY
            if (player is FortressServerPlayerEntity) {
                player.set_WasInBlueprintWorldWhenLoggedOut(inBlueprintWorldOnDisconnect)
            }
        })

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(AfterPlayerChange { player: ServerPlayerEntity, origin: ServerWorld, destination: ServerWorld ->
            if (destination.registryKey === BLUEPRINT_DIMENSION_KEY) {
                player.changeGameMode(GameMode.CREATIVE)
            } else if (origin.registryKey === BLUEPRINT_DIMENSION_KEY) {
                player.changeGameMode(FORTRESS)
            }
        })
    }

}