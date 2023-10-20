package net.remmintan.mods.minefortress.core.interfaces;

import net.minecraft.block.Block;
import net.remmintan.mods.minefortress.core.FortressGamemode;

public interface IFortressManager {

    boolean hasRequiredBuilding(String requirementId, int minCount);
    boolean hasRequiredBlock(Block block, boolean blueprint, int minCount);

    int getTotalColonistsCount();

    int getReservedPawnsCount();

    void setGamemode(FortressGamemode gamemode);
    boolean isCreative();

}
