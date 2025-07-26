package net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls;

import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IBaseTask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    BlockPos getCurrentTaskPos();

    @Nullable ITaskBlockInfo getNextBlock();

    void tick();

    boolean readyToTakeNewTask();

    boolean taskIsOfType(Class<? extends IBaseTask> taskClass);
}
