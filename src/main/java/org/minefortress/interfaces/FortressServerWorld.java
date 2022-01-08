package org.minefortress.interfaces;

import org.minefortress.blueprints.ServerBlueprintManager;
import org.minefortress.tasks.TaskManager;

public interface FortressServerWorld {

    TaskManager getTaskManager();
    ServerBlueprintManager getBlueprintManager();

}
