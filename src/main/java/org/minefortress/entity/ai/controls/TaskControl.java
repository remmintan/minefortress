package org.minefortress.entity.ai.controls;

import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.ITaskControl;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IBaseTask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.Colonist;
import org.minefortress.tasks.BlueprintTask;
import org.minefortress.tasks.CutTreesTask;
import org.minefortress.tasks.RoadsTask;
import org.minefortress.tasks.SimpleSelectionTask;

import java.util.Optional;
import java.util.UUID;

public class TaskControl implements ITaskControl {

    private final Colonist worker;
    private boolean doingEverydayTasks = false;
    private ITask task;
    private ITaskPart taskPart;

    public TaskControl(Colonist worker) {
        this.worker = worker;
    }

    @Override
    public void setTask(@NotNull IBaseTask exTask) {
        if (!(exTask instanceof ITask t))
            throw new IllegalArgumentException("TaskControl can only handle ITask tasks");
        this.task = t;
        this.taskPart = task.getNextPart(worker);
        this.updateCurrentTaskDesription();
    }

    @Override
    public void fail() {
        if(taskPart!=null)
            taskPart.returnTaskPart();
        this.resetTaskPart();
    }

    @Override
    public void success() {
        if(taskPart!=null)
            this.task.finishPart(taskPart, worker);

        if(this.taskPart != null && !this.taskPart.hasNext())
            this.findNextPart();
    }

    @Override
    public boolean hasTask() {
        return task != null && task.notCancelled();
    }

    @Override
    public boolean hasTaskPart() {
        return taskPart != null;
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
    public boolean partHasMoreBlocks() {
        return taskPart != null && taskPart.hasNext();
    }

    @Override
    public void findNextPart() {
        if(task.hasAvailableParts()) {
            this.setTask(task);
        } else {
            this.resetTaskPart();
        }
    }

    @Override
    public Optional<UUID> getTaskId() {
        return Optional.ofNullable(task).map(ITask::getId);
    }

    @Override
    @Nullable
    public ITaskBlockInfo getNextBlock() {
        return taskPart.next();
    }

    private void updateCurrentTaskDesription() {
        if (task instanceof SimpleSelectionTask) {
            if (task.getTaskType() == TaskType.REMOVE) {
                worker.setCurrentTaskDesc("Digging");
            } else {
                worker.setCurrentTaskDesc("Building");
            }
        } else if (task instanceof BlueprintTask) {
            worker.setCurrentTaskDesc("Building blueprint");
        } else if (task instanceof CutTreesTask) {
            worker.setCurrentTaskDesc("Falling trees");
        } else if (task instanceof RoadsTask) {
            worker.setCurrentTaskDesc("Building roads");
        }
    }

    private void resetTaskPart() {
        if (task != null && task.isComplete())
            this.task = null;
        this.taskPart = null;
    }

}
