package org.minefortress.entity.interfaces;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.fortress.server.FortressModServerManager;
import org.minefortress.interfaces.FortressServer;

import java.util.Optional;
import java.util.UUID;

public interface IFortressAwareEntity {

    MinecraftServer getServer();
    Optional<UUID> getMasterId();

    default Optional<FortressModServerManager> getFortressModServerManager() {
        final var server = getServer();
        if(server instanceof FortressServer fortressServer) {
            return Optional.of(fortressServer.getFortressModServerManager());
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

    default Optional<FortressServerManager> getFortressServerManager() {
        return getMasterId().flatMap(it -> getFortressModServerManager().map(fms -> fms.getByPlayerId(it)));
    }

}
