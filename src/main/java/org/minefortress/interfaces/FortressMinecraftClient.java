package org.minefortress.interfaces;

import org.minefortress.renderer.gui.FortressHud;
import org.minefortress.selections.SelectionManager;

public interface FortressMinecraftClient {

    SelectionManager getSelectionManager();
    FortressHud getFortressHud();
    boolean isNotFortressGamemode();
    boolean isFortressGamemode();

}
