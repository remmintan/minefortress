package net.remmintan.mods.minefortress.core.interfaces;

import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;

public interface IFortressManager {

    boolean hasRequiredBuilding(ProfessionType type, int level, int minCount);

    int getTotalColonistsCount();

    int getReservedPawnsCount();

    void setGamemode(FortressGamemode gamemode);
    boolean isCreative();

}
