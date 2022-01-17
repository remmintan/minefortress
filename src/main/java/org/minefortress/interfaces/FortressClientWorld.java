package org.minefortress.interfaces;

import org.minefortress.blueprints.BlueprintManager;
import org.minefortress.tasks.ClientTasksHolder;

public interface FortressClientWorld {

    ClientTasksHolder getClientTasksHolder();
    BlueprintManager getBlueprintManager();

}
