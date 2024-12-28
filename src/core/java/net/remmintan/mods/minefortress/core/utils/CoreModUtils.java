package net.remmintan.mods.minefortress.core.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IClientBlueprintManager;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IClientBuildingsManager;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientPawnsSelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.professions.IClientProfessionManager;
import net.remmintan.mods.minefortress.core.interfaces.renderers.IRenderersProvider;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IAreasClientManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IClientTasksHolder;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksInformationHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class CoreModUtils {

    public static IRenderersProvider getRenderersProvider() {
        final var client = MinecraftClient.getInstance();
        if (client instanceof IRenderersProvider renderersProvider) {
            return renderersProvider;
        }
        throw new IllegalStateException("MinecraftClient is not an instance of IRenderersProvider");
    }

    public static IClientManagersProvider getManagersProvider() {
        final var client = MinecraftClient.getInstance();
        if(client instanceof IClientManagersProvider managersProvider) {
            return managersProvider;
        }
        throw new IllegalStateException("MinecraftClient is not an instance of IMineFortressManagersProvider");
    }

    public static IServerManagersProvider getManagersProvider(ServerPlayerEntity player) {
        final var fortressServer = (IFortressServer) player.server;
        return fortressServer.get_FortressModServerManager().getManagersProvider(player);
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

    public static IClientFortressManager getFortressManager() {
        return getManagersProvider().get_ClientFortressManager();
    }

    public static IServerFortressManager getFortressManager(ServerPlayerEntity player) {
        final var fortressServer = (IFortressServer) player.server;
        return fortressServer.get_FortressModServerManager().getFortressManager(player);
    }

    public static IClientProfessionManager getProfessionManager() {
        return getFortressManager().getProfessionManager();
    }

    public static IClientPawnsSelectionManager getPawnsSelectionManager() {
        return getManagersProvider().get_PawnsSelectionManager();
    }

    public static IClientBlueprintManager getBlueprintManager() {
        return getManagersProvider().get_BlueprintManager();
    }

    public static IClientBuildingsManager getBuildingsManager() {
        return getManagersProvider().get_BuildingsManager();
    }

    public static ISelectionManager getSelectionManager() {
        return getManagersProvider().get_SelectionManager();
    }

    public static IAreasClientManager getAreasClientManager() {
        return getManagersProvider().get_AreasClientManager();
    }

    @NotNull
    public static ClientPlayerEntity getClientPlayer() {
        return Objects.requireNonNull(MinecraftClient.getInstance().player);
    }
}
