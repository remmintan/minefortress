package org.minefortress.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.interfaces.FortressServer;

abstract class MineFortressCommand {

    public abstract void register(CommandDispatcher<ServerCommandSource> dispatcher);

    public boolean clientSided() {
        return true;
    }

    protected static FortressServerManager getFortressServerManager(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var srvPlayer = context.getSource().getPlayer();
        final var server = (FortressServer) srvPlayer.getServer();
        if(server == null) {
            throw new RuntimeException("Server is null");
        }
        return server.getFortressModServerManager().getByPlayer(srvPlayer);
    }

}
