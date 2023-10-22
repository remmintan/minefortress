package org.minefortress.entity.ai.controls;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.ITaskControl;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.Colonist;
import org.minefortress.tasks.*;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TaskControl implements ITaskControl {

    private final Colonist colonist;

    private final Cache<UUID, Boolean> returnedIds = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();

    private boolean doingEverydayTasks = false;
    private ITask task;
    private ITaskPart taskPart;
    private Iterator<ITaskBlockInfo> blocks;
    private Consumer<ITaskPart> onTaskFailed;
    private Supplier<Boolean> cancelled;


    public TaskControl(Colonist colonist) {
        this.colonist = colonist;
    }

    @Override
    public boolean canStartTask(ITask task) {
        return !hasTask() && !this.returnedIds.asMap().containsKey(task.getId()) && colonist.getCurrentFoodLevel() > 0;
    }

    @Override
    public void setTask(@NotNull ITask task, ITaskPart taskPart, Consumer<ITaskPart> onTaskFailed, Supplier<Boolean> cancelled) {
        this.task = task;
        this.taskPart = taskPart;
        this.blocks = taskPart.getBlocks().iterator();
        this.onTaskFailed = onTaskFailed;
        this.cancelled = cancelled;
        this.updateCurrentTaskDesription();
    }

    private void updateCurrentTaskDesription() {
        if(task instanceof SimpleSelectionTask) {
            if(task.getTaskType() == TaskType.REMOVE) {
                colonist.setCurrentTaskDesc("Digging");
            } else {
                colonist.setCurrentTaskDesc("Building");
            }
        } else if(task instanceof BlueprintTask) {
            colonist.setCurrentTaskDesc("Building blueprint");
        } else if(task instanceof CutTreesTask) {
            colonist.setCurrentTaskDesc("Falling trees");
        } else if(task instanceof RoadsTask) {
            colonist.setCurrentTaskDesc("Building roads");
        }
    }

    @Override
    public void resetTask() {
        this.task = null;
        this.taskPart = null;
        this.blocks = null;
        this.onTaskFailed = null;
        this.cancelled = null;
    }

    @Override
    public void fail() {
        if(!hasTask()) return;
        this.onTaskFailed.accept(taskPart);
        this.returnedIds.put(task.getId(), true);
        this.resetTask();
    }

    @Override
    public void success() {
        if(!hasTask()) return;
        this.task.finishPart(taskPart, colonist);
        this.resetTask();
    }

    @Override
    public boolean hasTask() {
        if(cancelled != null && cancelled.get()) {
            this.resetTask();
            colonist.resetControls();
        }
        return task != null;
    }

    @Override
    public void setDoingEverydayTasks(boolean doingEverydayTasks) {
        this.doingEverydayTasks = doingEverydayTasks;
    }

    @Override
    public boolean isDoingEverydayTasks() {
        return doingEverydayTasks;
    }

    @Override
    public boolean is(TaskType type) {
        if(!hasTask()) return false;
        return task.getTaskType() == type;
    }

    @Override
    public boolean partHasMoreBlocks() {
        if(!hasTask()) return false;
        return blocks.hasNext();
    }

    @Override
    public Optional<UUID> getTaskId() {
        return Optional.ofNullable(task).map(ITask::getId);
    }

    @Override
    @Nullable
    public ITaskBlockInfo getNextBlock() {
        return blocks.next();
    }

    @Override
    public boolean isBlueprintTask() {
        return task instanceof BlueprintTask;
    }

}
