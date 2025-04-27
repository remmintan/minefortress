package net.remmintan.mods.minefortress.core.interfaces.tasks;

import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto;

import java.util.List;
import java.util.UUID;

public interface IClientTasksHolder extends ITasksModelBuilderInfoProvider, ITasksRenderInfoProvider {

    void addTasks(List<TaskInformationDto> tasks);

    void removeTask(UUID id);
    void addTask(UUID uuid, Iterable<BlockPos> blocks, TaskType type, UUID superTaskId);

    void cancelLatestTask();

    void toggleSelectionVisibility();
    boolean isSelectionHidden();

}
