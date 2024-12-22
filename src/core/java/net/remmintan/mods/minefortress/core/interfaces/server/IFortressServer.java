package net.remmintan.mods.minefortress.core.interfaces.server;

import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.IBlueprintWorld;

public interface IFortressServer {

    IFortressModServerManager get_FortressModServerManager();

    IBlueprintWorld get_BlueprintWorld();

}
