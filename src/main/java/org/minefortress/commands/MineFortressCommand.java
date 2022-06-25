package org.minefortress.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

interface MineFortressCommand {

    void register(CommandDispatcher<ServerCommandSource> dispatcher);

    default boolean clientSided() {
        return true;
    }

}
