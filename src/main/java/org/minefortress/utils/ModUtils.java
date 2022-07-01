package org.minefortress.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.MineFortressMod;

public class ModUtils {

    public static boolean isFortressGamemode(PlayerEntity player) {
        if(player instanceof ServerPlayerEntity serverPlayer) {
            return serverPlayer.interactionManager.getGameMode() == MineFortressMod.FORTRESS;
        }
        if(player instanceof ClientPlayerEntity clientPlayer) {
            final var interactionManager = MinecraftClient.getInstance().interactionManager;
            if(interactionManager != null)
                return interactionManager.getCurrentGameMode() == MineFortressMod.FORTRESS;
        }
        return false;
    }

}
