package org.minefortress.network.interfaces;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.interfaces.FortressServer;

public interface FortressC2SPacket extends FortressPacket {

    void handle(MinecraftServer server, ServerPlayerEntity player);

    default FortressServerManager getFortressServerManager(MinecraftServer server, ServerPlayerEntity player) {
        final var fortressServer = (FortressServer) server;
        return fortressServer.get_FortressModServerManager().getByPlayer(player);
    }

}
