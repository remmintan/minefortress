package net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls;

import net.remmintan.mods.minefortress.core.interfaces.tasks.IBaseTask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface ITaskControl {

    void setTask(@NotNull IBaseTask task);

    void fail();

    void success();

    boolean hasTask();

    boolean hasTaskPart();

    void setDoingEverydayTasks(boolean doingEverydayTasks);

    boolean isDoingEverydayTasks();

    boolean partHasMoreBlocks();

    void findNextPart();

    Optional<UUID> getTaskId();

    @Nullable ITaskBlockInfo getNextBlock();
}
