package org.minefortress.entity.ai.controls;

import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.ITaskControl;
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

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

public class TaskControl implements ITaskControl {

    private final Colonist worker;
    private boolean doingEverydayTasks = false;
    private ITask task;
    private ITaskPart taskPart;
    private Iterator<ITaskBlockInfo> blocks;

    public TaskControl(Colonist worker) {
        this.worker = worker;
    }

    @Override
    public boolean canStartTask(ITask task) {
        return worker.getCurrentFoodLevel() > 0;
    }

    @Override
    public void setTask(@NotNull ITask task) {
        this.task = task;
        this.taskPart = task.getNextPart(worker);
        this.blocks = taskPart.getBlocks().iterator();
        this.updateCurrentTaskDesription();
    }

    private void updateCurrentTaskDesription() {
        if(task instanceof SimpleSelectionTask) {
            if(task.getTaskType() == TaskType.REMOVE) {
                worker.setCurrentTaskDesc("Digging");
            } else {
                worker.setCurrentTaskDesc("Building");
            }
        } else if(task instanceof BlueprintTask) {
            worker.setCurrentTaskDesc("Building blueprint");
        } else if(task instanceof CutTreesTask) {
            worker.setCurrentTaskDesc("Falling trees");
        } else if(task instanceof RoadsTask) {
            worker.setCurrentTaskDesc("Building roads");
        }
    }

    @Override
    public void resetTask() {
        if(task!=null && task.taskFullyFinished())
            this.task = null;
        this.taskPart = null;
        this.blocks = null;
    }

    @Override
    public void fail() {
        if(!hasTask()) return;
        taskPart.returnTaskPart();
        this.resetTask();
    }

    @Override
    public void success() {
        if(!hasTask()) return;
        this.task.finishPart(taskPart, worker);
        if(task.hasAvailableParts()) {
            // taking next part into work
            this.setTask(task);
        } else {
            this.resetTask();
        }
    }

    @Override
    public boolean hasTask() {
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
