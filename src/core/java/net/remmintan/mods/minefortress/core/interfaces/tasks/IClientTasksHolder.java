package net.remmintan.mods.minefortress.core.interfaces.tasks;

import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto;

public interface IClientTasksHolder extends ITasksModelBuilderInfoProvider, ITasksRenderInfoProvider {

    void addTask(TaskInformationDto task);

    void removeTask(BlockPos pos);
    void cancelLatestTask();

    void toggleSelectionVisibility();
    boolean isSelectionHidden();

}
