package org.minefortress.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.fortress.server.FortressModServerManager;
import org.minefortress.interfaces.FortressServer;

import java.util.Optional;
import java.util.UUID;

public interface IFortressAwareEntity {

    MinecraftServer getServer();
    Optional<UUID> getFortressId();

    default Optional<FortressModServerManager> getFortressModServerManager() {
        final var server = getServer();
        if(server instanceof FortressServer fortressServer) {
            return Optional.of(fortressServer.getFortressModServerManager());
        } else {
            return Optional.empty();
        }
    }

    default Optional<ServerPlayerEntity> getMasterPlayer() {
        return getFortressModServerManager().flatMap(it -> getFortressId().flatMap(it::getPlayerByFortressId));
    }

    default void sendMessageToMasterPlayer(String message) {
        getMasterPlayer().ifPresent(it -> it.sendMessage(new LiteralText(message), false));
    }

    default boolean isScreenOpen(Class<? extends ScreenHandler> screenHandlerClass) {
        return getMasterPlayer()
                .map(it -> it.currentScreenHandler)
                .map(screenHandlerClass::isInstance)
                .orElse(false);
    }

    default Optional<FortressServerManager> getFortressServerManager() {
        return getFortressId().flatMap(it -> getFortressModServerManager().map(fms -> fms.getByFortressId(it)));
    }

}
