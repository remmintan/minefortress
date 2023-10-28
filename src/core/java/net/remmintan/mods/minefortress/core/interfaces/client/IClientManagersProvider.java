package net.remmintan.mods.minefortress.core.interfaces.client;

import net.remmintan.mods.minefortress.core.interfaces.blueprints.IClientBlueprintManager;
import net.remmintan.mods.minefortress.core.interfaces.infuence.IClientInfluenceManager;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IAreasClientManager;

public interface IClientManagersProvider {
    ISelectionManager get_SelectionManager();
    IAreasClientManager get_AreasClientManager();
    IClientBlueprintManager get_BlueprintManager();
    IClientFortressManager get_ClientFortressManager();
    IClientInfluenceManager get_InfluenceManager();
}
