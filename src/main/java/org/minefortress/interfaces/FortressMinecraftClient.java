package org.minefortress.interfaces;

import org.minefortress.blueprints.BlueprintDataManager;
import org.minefortress.blueprints.BlueprintManager;
import org.minefortress.renderer.gui.FortressHud;
import org.minefortress.selections.SelectionManager;

public interface FortressMinecraftClient {

    SelectionManager getSelectionManager();
    FortressHud getFortressHud();
    BlueprintManager getBlueprintManager();
    BlueprintDataManager getBlueprintDataManager();
    boolean isNotFortressGamemode();
    boolean isFortressGamemode();

}
