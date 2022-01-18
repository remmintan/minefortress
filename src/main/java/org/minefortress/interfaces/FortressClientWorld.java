package org.minefortress.interfaces;

import org.minefortress.blueprints.BlueprintManager;
import org.minefortress.tasks.ClientTasksHolder;
import org.minefortress.village.ColonistsManager;

public interface FortressClientWorld {

    ClientTasksHolder getClientTasksHolder();
    BlueprintManager getBlueprintManager();
    ColonistsManager getColonistsManager();

}
