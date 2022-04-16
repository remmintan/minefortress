package org.minefortress.tasks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.minefortress.network.ServerboundCancelTaskPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.selections.ClientSelection;

import java.util.*;

public class ClientTasksHolder {

    private final Map<UUID, UUID> subtasksMap = new HashMap<>();

    private final Map<UUID, ClientSelection> removeTasks = new HashMap<>();
    private final Map<UUID, ClientSelection> buildTasks = new HashMap<>();

    private final Stack<UUID> tasksStack = new Stack<>();

    private boolean selectionHidden = false;
    private boolean needRebuild = false;

    public void cancelTask() {
        if(tasksStack.empty()) return;
        final UUID lastTaskId = tasksStack.pop();
        subtasksMap.entrySet().stream().filter(it -> it.getValue().equals(lastTaskId)).map(Map.Entry::getKey).forEach(it -> {
            removeTask(it);
            FortressClientNetworkHelper.send(FortressChannelNames.CANCEL_TASK, new ServerboundCancelTaskPacket(it));
        });
        removeTask(lastTaskId);
        FortressClientNetworkHelper.send(FortressChannelNames.CANCEL_TASK, new ServerboundCancelTaskPacket(lastTaskId));
    }

    public void cancelAllTasks() {
        while(!tasksStack.empty()) {
            cancelTask();
        }
    }

    public void addTask(UUID uuid, Iterable<BlockPos> blocks) {
        addTask(uuid, blocks, Blocks.DIRT.getDefaultState(), TaskType.BUILD);
    }

    public void addTask(UUID uuid, Iterable<BlockPos> blocks, BlockState blockState, TaskType type) {
        addTask(uuid, blocks, blockState, type, null);
    }

    public void addTask(UUID uuid, Iterable<BlockPos> blocks, BlockState blockState, TaskType type, UUID superTaskId) {
        if(blockState == null) {
            blockState = Blocks.DIRT.getDefaultState();
        }
        ClientSelection newTask = new ClientSelection(blocks, blockState);
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

    public Set<ClientSelection> getAllRemoveTasks() {
        return new HashSet<>(removeTasks.values());
    }

    public Set<ClientSelection> getAllBuildTasks() {
        return new HashSet<>(buildTasks.values());
    }

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

    public boolean isNeedRebuild() {
        return needRebuild;
    }

    public void setNeedRebuild(boolean needRebuild) {
        this.needRebuild = needRebuild;
    }

    public void toggleSelectionVisibility() {
        this.selectionHidden = !this.selectionHidden;
    }

    public boolean isSelectionHidden() {
        return selectionHidden;
    }

    public boolean isEmpty() {
        return buildTasks.isEmpty() && removeTasks.isEmpty();
    }
}
