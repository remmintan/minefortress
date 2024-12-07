package net.remmintan.mods.minefortress.core

import com.chocohead.mm.api.ClassTinkerers
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode

val FORTRESS: GameMode = ClassTinkerers.getEnum(GameMode::class.java, "FORTRESS")

fun isFortressGamemode(player: PlayerEntity?): Boolean {
    if (player is ServerPlayerEntity) {
        return player.interactionManager.gameMode == FORTRESS
    }
    if (player is ClientPlayerEntity) {
        return isClientInFortressGamemode()
    }
    return false
}

fun isFortressGamemode(livingEntity: LivingEntity?): Boolean {
    if (livingEntity is PlayerEntity) {
        return isFortressGamemode(livingEntity)
    }
    return false
}

fun isClientInFortressGamemode(): Boolean {
    val interactionManager = MinecraftClient.getInstance().interactionManager
    return interactionManager != null && interactionManager.currentGameMode == FORTRESS
}