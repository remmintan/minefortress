package org.minefortress.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class CampfireToggleCommand extends MineFortressCommand {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("fortress").then(literal("campfire")
                .then(argument("enabled", BoolArgumentType.bool())
                    .executes(context -> {
                        final var fortressServerManager = getServerFortressManager(context);
                        final var campfireEnabled = BoolArgumentType.getBool(context, "enabled");
                        fortressServerManager.setCampfireVisibilityState(campfireEnabled);
                        return 1;
                    })
                )
            )
        );
    }

    @Override
    public boolean clientSided() {
        return false;
    }
}
