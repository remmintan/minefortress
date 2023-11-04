package org.minefortress.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import java.util.Arrays;
import java.util.List;

public class CommandsManager {

    private static final List<MineFortressCommand> commands = Arrays.asList(
            new DebugItemsCommand(),
            new DebugPawnsCommand(),
            new CampfireToggleCommand()
    );

    private CommandsManager(){}

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            for(var command: commands){
                if(command.clientSided() && environment.dedicated) continue;
                command.register(dispatcher);
            }
        });
    }

}
