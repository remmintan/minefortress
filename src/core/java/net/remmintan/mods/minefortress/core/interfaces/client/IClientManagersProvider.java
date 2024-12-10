package net.remmintan.mods.minefortress.core.interfaces.client;

import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintsImportExportManager;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IClientBlueprintManager;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IClientBuildingsManager;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientPawnsSelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.combat.ITargetedSelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IAreasClientManager;

public interface IClientManagersProvider {
    ISelectionManager get_SelectionManager();
    IAreasClientManager get_AreasClientManager();
    IClientBlueprintManager get_BlueprintManager();
    IClientFortressManager get_ClientFortressManager();
    IClientPawnsSelectionManager get_PawnsSelectionManager();

    IBlueprintsImportExportManager get_BlueprintsImportExportManager();

    IClientBuildingsManager get_BuildingsManager();
    default ISelectedColonistProvider getSelectedColonistProvider() {
        final var pawnsSelectionManager = get_PawnsSelectionManager();
        if (pawnsSelectionManager instanceof ISelectedColonistProvider) {
            return (ISelectedColonistProvider) pawnsSelectionManager;
        }
        throw new IllegalStateException("The pawns selection manager is not an instance of ISelectedColonistProvider");
    }

    default ITargetedSelectionManager getTargetedSelectionManager() {
        final var pawnsSelectionManager = get_PawnsSelectionManager();
        if (pawnsSelectionManager instanceof ITargetedSelectionManager) {
            return (ITargetedSelectionManager) pawnsSelectionManager;
        }
        throw new IllegalStateException("The pawns selection manager is not an instance of ITargetedSelectionManager");
    }

}
