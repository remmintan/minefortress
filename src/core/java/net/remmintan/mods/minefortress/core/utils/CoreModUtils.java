package net.remmintan.mods.minefortress.core.utils;

import net.minecraft.client.MinecraftClient;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IClientTasksHolder;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksInformationHolder;

import java.util.Optional;

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

    public static Optional<IClientTasksHolder> getClientTasksHolder() {
        return Optional
                .ofNullable(MinecraftClient.getInstance())
                .map(it -> it.world)
                .map(ITasksInformationHolder.class::cast)
                .map(ITasksInformationHolder::get_ClientTasksHolder);
    }

}
