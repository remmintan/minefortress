package org.minefortress.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressModServerManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;

abstract class MineFortressCommand {

    public abstract void register(CommandDispatcher<ServerCommandSource> dispatcher);

    public boolean clientSided() {
        return true;
    }

    protected static IServerManagersProvider getServerManagersProvider(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var srvPlayer = context.getSource().getPlayerOrThrow();
        final var modServerManager = getFortressModServerManager(srvPlayer);
        return modServerManager.getManagersProvider(srvPlayer);
    }

    private static IFortressModServerManager getFortressModServerManager(ServerPlayerEntity srvPlayer) {
        final var server = (IFortressServer) srvPlayer.getServer();
        if(server == null) {
            throw new RuntimeException("Server is null");
        }
        return server.get_FortressModServerManager();
    }

    protected static IServerFortressManager getServerFortressManager(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var srvPlayer = context.getSource().getPlayerOrThrow();
        final IFortressModServerManager modServerManager = getFortressModServerManager(srvPlayer);
        return modServerManager.getFortressManager(srvPlayer);
    }

}
