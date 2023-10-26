package net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls;

import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ITaskControl {
    boolean canStartTask(ITask task);

    void setTask(@NotNull ITask task, ITaskPart taskPart, Consumer<ITaskPart> onTaskFailed, Supplier<Boolean> cancelled);

    void resetTask();

    void fail();

    void success();

    boolean hasTask();

    void setDoingEverydayTasks(boolean doingEverydayTasks);

    boolean isDoingEverydayTasks();

    boolean is(TaskType type);

    boolean partHasMoreBlocks();

    Optional<UUID> getTaskId();

    @Nullable ITaskBlockInfo getNextBlock();

    boolean isBlueprintTask();
}
