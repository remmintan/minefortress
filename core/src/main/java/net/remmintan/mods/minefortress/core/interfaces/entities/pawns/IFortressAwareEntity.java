package net.remmintan.mods.minefortress.core.interfaces.entities.pawns;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressModServerManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;

import java.util.Optional;
import java.util.UUID;

public interface IFortressAwareEntity {

    MinecraftServer getServer();
    Optional<UUID> getMasterId();

    default Optional<IFortressModServerManager> getFortressModServerManager() {
        final var server = getServer();
        if(server instanceof IFortressServer IFortressServer) {
            return Optional.of(IFortressServer.get_FortressModServerManager());
        } else {
            return Optional.empty();
        }
    }

    default Optional<ServerPlayerEntity> getMasterPlayer() {
        return getMasterId().map(it -> getServer().getPlayerManager().getPlayer(it));
    }

    default void sendMessageToMasterPlayer(String message) {
        getMasterPlayer().ifPresent(it -> it.sendMessage(Text.literal(message), false));
    }

    default boolean isScreenOpen(Class<? extends ScreenHandler> screenHandlerClass) {
        return getMasterPlayer()
                .map(it -> it.currentScreenHandler)
                .map(screenHandlerClass::isInstance)
                .orElse(false);
    }

    default Optional<IServerFortressManager> getFortressServerManager() {
        return getMasterId().flatMap(it -> getFortressModServerManager().map(fms -> fms.getFortressManager(it)));
    }

}
