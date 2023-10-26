package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.server.WorldGenerationProgressListener;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.IBlueprintsWorld;

public interface IFortressServer {

    IBlueprintsWorld get_BlueprintsWorld();
    WorldGenerationProgressListener get_WorldGenerationProgressListener();
    IFortressModServerManager get_FortressModServerManager();

}
