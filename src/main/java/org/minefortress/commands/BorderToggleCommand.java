package org.minefortress.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BorderToggleCommand extends MineFortressCommand {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("fortress").then(
                literal("border").then(argument("enabled", BoolArgumentType.bool())
                    .executes(context -> {
                        final var fortressServerManager = getServerFortressManager(context);
                        final var borderEnabled = BoolArgumentType.getBool(context, "enabled");
                        fortressServerManager.setBorderVisibilityState(borderEnabled);
                        return 1;
                    })
                )
            )
        );
    }

}
