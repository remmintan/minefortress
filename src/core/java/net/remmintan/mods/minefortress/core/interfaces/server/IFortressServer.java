package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.server.WorldGenerationProgressListener;

public interface IFortressServer {

    WorldGenerationProgressListener get_WorldGenerationProgressListener();
    IFortressModServerManager get_FortressModServerManager();

}
