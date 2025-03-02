package net.remmintan.mods.minefortress.core.interfaces.server;

import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.IBlueprintWorld;

public interface IFortressServer {


    FortressGamemode get_FortressGamemode();
    IBlueprintWorld get_BlueprintWorld();

}
