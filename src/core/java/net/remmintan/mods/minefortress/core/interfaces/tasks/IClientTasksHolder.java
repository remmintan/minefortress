package net.remmintan.mods.minefortress.core.interfaces.tasks;

import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto;

import java.util.List;

public interface IClientTasksHolder extends ITasksModelBuilderInfoProvider, ITasksRenderInfoProvider {

    void addTasks(List<TaskInformationDto> tasks);
    void cancelLatestTask();

    void toggleSelectionVisibility();
    boolean isSelectionHidden();

}
