package org.minefortress.fortress;

import net.minecraft.block.Block;
import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.interfaces.resources.IResourceManager;

public interface IFortressManager {

    boolean hasRequiredBuilding(String requirementId, int minCount);
    boolean hasRequiredBlock(Block block, boolean blueprint, int minCount);

    IResourceManager getResourceManager();

    int getTotalColonistsCount();

    int getReservedPawnsCount();

    void setGamemode(FortressGamemode gamemode);
    boolean isCreative();

}
