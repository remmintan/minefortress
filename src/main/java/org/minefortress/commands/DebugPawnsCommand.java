package org.minefortress.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DebugPawnsCommand extends MineFortressCommand {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("fortress")
                        .then(literal("pawns")
                                .then(argument("num", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int num = IntegerArgumentType.getInteger(context, "num");
                                            final var fortressServerManager = getServerFortressManager(context);
                                            for (int i = 0; i < num; i++) {
                                                fortressServerManager.spawnPawnNearCampfire();
                                            }
                                            return 1;
                                        })
                                )
                        )
        );

        // kill all
        dispatcher.register(
                literal("fortress")
                        .then(literal("pawns")
                                .then(literal("kill")
                                        .executes(context -> {
                                            final var fortressServerManager = getServerFortressManager(context);
                                            fortressServerManager.killAllPawns();
                                            return 1;
                                        })
                                )
                        )
        );
    }

}
