package org.minefortress.commands;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import java.util.Arrays;
import java.util.List;

public class CommandsManager {

    private static final List<MineFortressCommand> commands = Arrays.asList(
            new DebugItemsCommand(),
            new DebugPawnsCommand()
    );

    private CommandsManager(){}

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            for(var command: commands){
                if(command.clientSided() && dedicated) continue;
                command.register(dispatcher);
            }
        });
    }

}
