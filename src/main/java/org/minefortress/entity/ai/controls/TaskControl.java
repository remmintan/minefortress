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

    private int cooldown = 0;

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
        this.resetAll();
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
        return Optional.ofNullable(task).map(ITask::getPos);
    }

    @Override
    @Nullable
    public ITaskBlockInfo getNextBlock() {
        return taskPart.next();
    }

    @Override
    public void tick() {
        if (cooldown > 0) cooldown--;
    }

    @Override
    public boolean readyToTakeNewTask() {
        return cooldown <= 0 && !hasTask() && !isDoingEverydayTasks();
    }

    public boolean taskIsOfType(Class<? extends IBaseTask> taskClass) {
        return taskClass.isInstance(task);
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
        if (task != null && task.isComplete()) {
            resetAll();
        }

        this.taskPart = null;
    }

    private void resetAll() {
        if (task != null) {
            this.task.removeWorker();
            this.task = null;
        }
        this.taskPart = null;
        cooldown = 20;
    }

}
