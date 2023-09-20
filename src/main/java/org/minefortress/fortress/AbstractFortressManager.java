package org.minefortress.fortress;

import net.minecraft.block.Block;
import org.minefortress.fortress.resources.FortressResourceManager;

public abstract class AbstractFortressManager {

    public abstract boolean hasRequiredBuilding(String requirementId, int minCount);
    public abstract boolean hasRequiredBlock(Block block, boolean blueprint, int minCount);

    public abstract FortressResourceManager getResourceManager();

    public abstract int getTotalColonistsCount();

    public abstract int getReservedPawnsCount();

    abstract void setGamemode(FortressGamemode gamemode);
    public abstract boolean isCreative();

}
