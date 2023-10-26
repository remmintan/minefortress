package net.remmintan.mods.minefortress.core.interfaces.tasks;

import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;

import java.util.List;
import java.util.UUID;

public interface IClientTasksHolder extends ITasksModelBuilderInfoProvider, ITasksRenderInfoProvider {

    void addRoadsSelectionTask(UUID digTaskId, UUID placeTaskId, List<BlockPos> positions);
    void addTask(UUID uuid, Iterable<BlockPos> blocks);
    void removeTask(UUID id);
    void addTask(UUID uuid, Iterable<BlockPos> blocks, TaskType type);
    void addTask(UUID uuid, Iterable<BlockPos> blocks, TaskType type, UUID superTaskId);

    void cancelAllTasks();
    void cancelTask();

    void toggleSelectionVisibility();
    boolean isSelectionHidden();

}
