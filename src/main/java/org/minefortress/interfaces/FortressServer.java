package org.minefortress.interfaces;

import net.minecraft.server.WorldGenerationProgressListener;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.fortress.server.FortressModServerManager;

public interface FortressServer {

    BlueprintsWorld get_BlueprintsWorld();
    WorldGenerationProgressListener get_WorldGenerationProgressListener();
    FortressModServerManager get_FortressModServerManager();

}
