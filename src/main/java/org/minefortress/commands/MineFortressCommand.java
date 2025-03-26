package org.minefortress.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.entities.player.IFortressPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;

abstract class MineFortressCommand {

    public abstract void register(CommandDispatcher<ServerCommandSource> dispatcher);

    public boolean clientSided() {
        return true;
    }

    protected static BlockPos getFortressPos(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var fortressPlayer = (IFortressPlayerEntity) context.getSource().getPlayerOrThrow();
        return fortressPlayer.get_FortressPos().orElseThrow();
    }


    protected static IServerManagersProvider getServerManagersProvider(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var srvPlayer = context.getSource().getPlayerOrThrow();
        return ServerModUtils.getManagersProvider(srvPlayer).orElseThrow();
    }

    protected static IServerFortressManager getServerFortressManager(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var srvPlayer = context.getSource().getPlayerOrThrow();
        return ServerModUtils.getFortressManager(srvPlayer).orElseThrow();
    }

}
