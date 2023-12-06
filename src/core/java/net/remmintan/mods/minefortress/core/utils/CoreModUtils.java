package net.remmintan.mods.minefortress.core.utils;

import net.minecraft.client.MinecraftClient;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;

public class CoreModUtils {

    public static IClientManagersProvider getMineFortressManagersProvider() {
        final var client = MinecraftClient.getInstance();
        if(client instanceof IClientManagersProvider managersProvider) {
            return managersProvider;
        }
        throw new IllegalStateException("MinecraftClient is not an instance of IMineFortressManagersProvider");
    }

    public static boolean isPlayerInCreative(IFortressAwareEntity colonist) {
        return colonist
                .getServerFortressManager()
                .map(IServerFortressManager::isCreative)
                .orElse(false);
    }

}
