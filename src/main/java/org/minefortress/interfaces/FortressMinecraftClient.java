package org.minefortress.interfaces;

import org.minefortress.blueprints.BlueprintMetadataManager;
import org.minefortress.blueprints.BlueprintManager;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.renderer.gui.FortressHud;
import org.minefortress.selections.SelectionManager;

public interface FortressMinecraftClient {

    SelectionManager getSelectionManager();
    FortressHud getFortressHud();
    BlueprintManager getBlueprintManager();
    BlueprintMetadataManager getBlueprintMetadataManager();
    FortressClientManager getFortressClientManager();
    boolean isNotFortressGamemode();
    boolean isFortressGamemode();

}
