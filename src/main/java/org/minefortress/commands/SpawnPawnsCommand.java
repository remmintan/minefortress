package org.minefortress.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class SpawnPawnsCommand extends MineFortressCommand {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("fortress")
                .then(literal("spawnPawns")
                    .then(argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            final var fortressServerManager = getServerFortressManager(context);
                            final var pawnsEnabled = BoolArgumentType.getBool(context, "enabled");
                            fortressServerManager.setSpawnPawns(pawnsEnabled);
                            return 1;
                        })
                    )
                )
        );
    }

}
