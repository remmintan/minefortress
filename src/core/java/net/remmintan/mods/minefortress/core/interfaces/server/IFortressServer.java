package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.server.WorldGenerationProgressListener;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.IBlueprintWorld;

public interface IFortressServer {

    WorldGenerationProgressListener get_WorldGenerationProgressListener();
    IFortressModServerManager get_FortressModServerManager();

    IBlueprintWorld get_BlueprintWorld();

}
