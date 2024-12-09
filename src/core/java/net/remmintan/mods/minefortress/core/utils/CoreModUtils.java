package net.remmintan.mods.minefortress.core.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IClientBlueprintManager;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IClientBuildingsManager;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.professions.IClientProfessionManager;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IAreasClientManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IClientTasksHolder;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksInformationHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
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

    private static IClientManagersProvider getClientManagersProvider() {
        return (IClientManagersProvider) MinecraftClient.getInstance();
    }

    public static IClientFortressManager getFortressClientManager() {
        return getClientManagersProvider().get_ClientFortressManager();
    }

    public static IClientProfessionManager getProfessionManager() {
        return getFortressClientManager().getProfessionManager();
    }

    public static IClientBlueprintManager getBlueprintManager() {
        return getClientManagersProvider().get_BlueprintManager();
    }

    public static IClientBuildingsManager getBuildingsManager() {
        return getClientManagersProvider().get_BuildingsManager();
    }

    public static ISelectionManager getSelectionManager() {
        return getClientManagersProvider().get_SelectionManager();
    }

    public static IAreasClientManager getAreasClientManager() {
        return getClientManagersProvider().get_AreasClientManager();
    }

    @NotNull
    public static ClientPlayerEntity getClientPlayer() {
        return Objects.requireNonNull(MinecraftClient.getInstance().player);
    }
}
