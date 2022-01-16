package org.minefortress.interfaces;

import org.minefortress.blueprints.BlueprintMetadataManager;
import org.minefortress.blueprints.BlueprintManager;
import org.minefortress.renderer.gui.FortressHud;
import org.minefortress.selections.SelectionManager;

public interface FortressMinecraftClient {

    SelectionManager getSelectionManager();
    FortressHud getFortressHud();
    BlueprintManager getBlueprintManager();
    BlueprintMetadataManager getBlueprintMetadataManager();
    boolean isNotFortressGamemode();
    boolean isFortressGamemode();

}
