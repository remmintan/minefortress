package org.minefortress.tasks;

import net.minecraft.util.math.BlockPos;
import net.remmintan.gobi.ClientTask;
import net.remmintan.mods.minefortress.building.BuildingHelper;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IClientTask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IClientTasksHolder;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksModelBuilderInfoProvider;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksRenderInfoProvider;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundCancelTaskPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.joml.Vector4f;

import java.util.*;

public class ClientTasksHolder implements ITasksModelBuilderInfoProvider, ITasksRenderInfoProvider, IClientTasksHolder {
    private static final Vector4f DESTROY_COLOR = new Vector4f(170f/255f, 0, 0, 1f);
    private static final Vector4f BUILD_COLOR = new Vector4f(0, 170f/255f, 0, 1f);

    private final Map<UUID, UUID> subtasksMap = new HashMap<>();

    private final Map<UUID, IClientTask> removeTasks = new HashMap<>();
    private final Map<UUID, IClientTask> buildTasks = new HashMap<>();

    private final Stack<UUID> tasksStack = new Stack<>();

    private boolean selectionHidden = false;
    private boolean needRebuild = false;

    @Override
    public void cancelLatestTask() {
        if(tasksStack.empty()) return;
        final UUID lastTaskId = tasksStack.pop();
        subtasksMap.entrySet().stream().filter(it -> it.getValue().equals(lastTaskId)).map(Map.Entry::getKey).forEach(it -> {
            removeTask(it);
            FortressClientNetworkHelper.send(FortressChannelNames.CANCEL_TASK, new ServerboundCancelTaskPacket(it));
        });
        removeTask(lastTaskId);
        FortressClientNetworkHelper.send(FortressChannelNames.CANCEL_TASK, new ServerboundCancelTaskPacket(lastTaskId));
        if(tasksStack.empty()) {
            ClientModUtils.getManagersProvider().get_PawnsSelectionManager().resetSelection();
        }
    }

    @Override
    public void cancelAllTasks() {
        while(!tasksStack.empty()) {
            cancelLatestTask();
        }
    }

    @Override
    public void addTasks(List<TaskInformationDto> tasks) {
        for(TaskInformationDto task: tasks) {
            addTask(task.id(), task.positions(), task.type(), null);
        }
    }

    @Override
    public void addTask(UUID uuid, Iterable<BlockPos> blocks, TaskType type, UUID superTaskId) {
        IClientTask newTask = new ClientTask(
                blocks,
                type == TaskType.REMOVE ? DESTROY_COLOR: BUILD_COLOR,
                (w, p) -> type == TaskType.REMOVE ? BuildingHelper.canRemoveBlock(w, p) : BuildingHelper.canPlaceBlock(w, p)
        );
        if(superTaskId != null) {
            subtasksMap.put(uuid, superTaskId);
        }

        if(type == TaskType.REMOVE) {
            removeTasks.put(uuid, newTask);
        } else {
            buildTasks.put(uuid, newTask);
        }

        tasksStack.push(uuid);
        this.setNeedRebuild(true);
    }

    @Override
    public Set<IClientTask> getAllSelections() {
        final var clientSelections = new HashSet<>(buildTasks.values());
        clientSelections.addAll(removeTasks.values());
        return clientSelections;
    }

    @Override
    public void removeTask(UUID uuid) {
        if(buildTasks.containsKey(uuid)) {
            buildTasks.remove(uuid);
        } else {
            removeTasks.remove(uuid);
        }

        subtasksMap.remove(uuid);
        tasksStack.remove(uuid);

        this.setNeedRebuild(true);
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
        return !selectionHidden && (!removeTasks.isEmpty() || !buildTasks.isEmpty());
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
