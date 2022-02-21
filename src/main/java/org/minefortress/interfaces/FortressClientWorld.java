package org.minefortress.interfaces;

import org.minefortress.blueprints.manager.ClientBlueprintManager;
import org.minefortress.blueprints.renderer.BlueprintRenderer;
import org.minefortress.tasks.ClientTasksHolder;

public interface FortressClientWorld {

    ClientTasksHolder getClientTasksHolder();
    ClientBlueprintManager getBlueprintManager();
    BlueprintRenderer getBlueprintRenderer();

}
