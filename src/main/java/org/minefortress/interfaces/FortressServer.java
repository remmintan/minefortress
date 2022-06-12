package org.minefortress.interfaces;

import net.minecraft.server.WorldGenerationProgressListener;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.fortress.server.FortressModServerManager;

public interface FortressServer {

    BlueprintsWorld getBlueprintsWorld();
    WorldGenerationProgressListener getWorldGenerationProgressListener();
    void setTicksMultiplier(int multiplier);
    FortressModServerManager getFortressModServerManager();

}
