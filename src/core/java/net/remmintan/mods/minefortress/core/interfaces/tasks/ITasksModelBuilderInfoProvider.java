package net.remmintan.mods.minefortress.core.interfaces.tasks;

import java.util.Set;

public interface ITasksModelBuilderInfoProvider {

    boolean isNeedRebuild();
    void setNeedRebuild(boolean rebuildNeeded);

    Set<IClientTask> getAllSelections();

}
