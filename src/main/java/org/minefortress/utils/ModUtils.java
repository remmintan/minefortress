package org.minefortress.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.MineFortressMod;
import org.minefortress.interfaces.FortressMinecraftClient;

public class ModUtils {

    public static boolean isFortressGamemode(PlayerEntity player) {
        if(player instanceof ServerPlayerEntity serverPlayer) {
            return serverPlayer.interactionManager.getGameMode() == MineFortressMod.FORTRESS;
        }
        if(player instanceof ClientPlayerEntity) {
            return isClientInFortressGamemode();
        }
        return false;
    }

    public static boolean isFortressGamemode(LivingEntity livingEntity) {
        if(livingEntity instanceof PlayerEntity player) {
            return isFortressGamemode(player);
        }
        return false;
    }

    public static FortressMinecraftClient getFortressClient() {
        return (FortressMinecraftClient) MinecraftClient.getInstance();
    }

    public static boolean isClientInFortressGamemode() {
        final var interactionManager = MinecraftClient.getInstance().interactionManager;
        return interactionManager != null && interactionManager.getCurrentGameMode() == MineFortressMod.FORTRESS;
    }

}
