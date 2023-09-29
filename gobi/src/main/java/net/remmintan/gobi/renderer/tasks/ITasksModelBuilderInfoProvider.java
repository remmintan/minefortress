package org.minefortress.selections.renderer.tasks;

import org.minefortress.selections.ClientSelection;

import java.util.Set;

public interface ITasksModelBuilderInfoProvider {

    boolean isNeedRebuild();
    void setNeedRebuild(boolean rebuildNeeded);

    Set<ClientSelection> getAllSelections();

}
