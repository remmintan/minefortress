package net.remmintan.mods.minefortress.core.interfaces.networking;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;

public interface FortressC2SPacket extends FortressPacket {

    void handle(MinecraftServer server, ServerPlayerEntity player);

    default IServerManagersProvider getManagersProvider(MinecraftServer server, ServerPlayerEntity player) {
        final var fortressServer = (IFortressServer) server;
        return fortressServer.get_FortressModServerManager().getManagersProvider(player);
    }

    default IServerFortressManager getFortressManager(MinecraftServer server, ServerPlayerEntity player) {
        final var fortressServer = (IFortressServer) server;
        return fortressServer.get_FortressModServerManager().getFortressManager(player);
    }

}
