package org.minefortress.tasks;

import net.minecraft.util.math.BlockPos;
import net.remmintan.gobi.ClientTask;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IClientTask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IClientTasksHolder;
import net.remmintan.mods.minefortress.core.utils.BuildingHelper;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundCancelTaskPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.joml.Vector4f;

import java.util.*;

public class ClientTasksHolder implements IClientTasksHolder {
    private static final Vector4f DESTROY_COLOR = new Vector4f(170f/255f, 0, 0, 1f);
    private static final Vector4f BUILD_COLOR = new Vector4f(0, 170f/255f, 0, 1f);

    private final Map<BlockPos, IClientTask> tasks = new HashMap<>();

    private final Stack<BlockPos> tasksStack = new Stack<>();

    private boolean selectionHidden = false;
    private boolean needRebuild = false;

    @Override
    public void cancelLatestTask() {
        if(tasksStack.empty()) return;
        final BlockPos pos = tasksStack.pop();

        tasks.remove(pos);
        tasksStack.remove(pos);

        this.setNeedRebuild(true);
        FortressClientNetworkHelper.send(FortressChannelNames.CANCEL_TASK, new ServerboundCancelTaskPacket(pos));
        if(tasksStack.empty()) {
            ClientModUtils.getManagersProvider().get_PawnsSelectionManager().resetSelection();
        }
    }

    @Override
    public void addTasks(List<TaskInformationDto> tasks) {
        for(TaskInformationDto task: tasks) {
            addTask(task.pos(), task.positions(), task.type());
        }
    }

    private void addTask(BlockPos taskPos, Iterable<BlockPos> blocks, TaskType type) {
        IClientTask newTask = new ClientTask(
                blocks,
                type == TaskType.REMOVE ? DESTROY_COLOR: BUILD_COLOR,
                (w, p) -> type == TaskType.REMOVE ? BuildingHelper.canRemoveBlock(w, p) : BuildingHelper.canPlaceBlock(w, p)
        );


        tasks.put(taskPos, newTask);

        tasksStack.push(taskPos);
        this.setNeedRebuild(true);
    }

    @Override
    public Set<IClientTask> getAllSelections() {
        return new HashSet<>(tasks.values());
    }

    @Override
    public boolean isNeedRebuild() {
        return needRebuild;
    }

    @Override
    public void setNeedRebuild(boolean needRebuild) {
        this.needRebuild = needRebuild;
    }

    @Override
    public boolean shouldRender() {
        return !selectionHidden && !tasks.isEmpty();
    }

    @Override
    public void toggleSelectionVisibility() {
        this.selectionHidden = !this.selectionHidden;
    }

    @Override
    public boolean isSelectionHidden() {
        return selectionHidden;
    }
}
