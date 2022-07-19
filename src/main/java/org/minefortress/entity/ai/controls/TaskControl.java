package org.minefortress.entity.ai.controls;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.Colonist;
import org.minefortress.tasks.*;
import org.minefortress.tasks.block.info.TaskBlockInfo;
import org.minefortress.tasks.interfaces.Task;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TaskControl {

    private final Colonist colonist;

    private final Cache<UUID, Boolean> returnedIds = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();

    private boolean doingEverydayTasks = false;
    private Task task;
    private TaskPart taskPart;
    private Iterator<TaskBlockInfo> blocks;
    private Consumer<TaskPart> onTaskFailed;
    private Supplier<Boolean> cancelled;


    public TaskControl(Colonist colonist) {
        this.colonist = colonist;
    }

    public boolean canStartTask(Task task) {
        return !hasTask() && !this.returnedIds.asMap().containsKey(task.getId()) && colonist.getCurrentFoodLevel() > 0;
    }

    public void setTask(@NotNull Task task, TaskPart taskPart, Consumer<TaskPart> onTaskFailed, Supplier<Boolean> cancelled) {
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

    public void resetTask() {
        this.task = null;
        this.taskPart = null;
        this.blocks = null;
        this.onTaskFailed = null;
        this.cancelled = null;
    }

    public void fail() {
        if(!hasTask()) return;
        this.onTaskFailed.accept(taskPart);
        this.returnedIds.put(task.getId(), true);
        this.resetTask();
    }

    public void success() {
        if(!hasTask()) return;
        this.task.finishPart(taskPart, colonist);
        this.resetTask();
    }

    public boolean hasTask() {
        if(cancelled != null && cancelled.get()) {
            this.resetTask();
            colonist.resetControls();
        }
        return task != null;
    }

    public void setDoingEverydayTasks(boolean doingEverydayTasks) {
        this.doingEverydayTasks = doingEverydayTasks;
    }

    public boolean isDoingEverydayTasks() {
        return doingEverydayTasks;
    }

    public boolean is(TaskType type) {
        if(!hasTask()) return false;
        return task.getTaskType() == type;
    }

    public boolean finished() {
        if(!hasTask()) return true;
        return !blocks.hasNext();
    }

    public UUID getTaskId() {
        return task.getId();
    }

    @Nullable
    public TaskBlockInfo getNextBlock() {
        return blocks.next();
    }

    public boolean isBlueprintTask() {
        return task instanceof BlueprintTask;
    }

}
