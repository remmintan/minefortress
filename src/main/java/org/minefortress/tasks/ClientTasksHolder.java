package org.minefortress.tasks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vector4f;
import org.minefortress.network.c2s.ServerboundCancelTaskPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.selections.ClientSelection;
import org.minefortress.selections.renderer.tasks.ITasksModelBuilderInfoProvider;
import org.minefortress.selections.renderer.tasks.ITasksRenderInfoProvider;
import org.minefortress.utils.BuildingHelper;

import java.util.*;

public class ClientTasksHolder implements ITasksModelBuilderInfoProvider, ITasksRenderInfoProvider {
    private static final Vector4f DESTROY_COLOR = new Vector4f(170f/255f, 0, 0, 1f);
    private static final Vector4f BUILD_COLOR = new Vector4f(0, 170f/255f, 0, 1f);

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
        ClientSelection newTask = new ClientSelection(
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
    public Set<ClientSelection> getAllSelections() {
        final var clientSelections = new HashSet<>(buildTasks.values());
        clientSelections.addAll(removeTasks.values());
        return clientSelections;
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

    @Override
    public boolean shouldRender() {
        return !selectionHidden && removeTasks.size() > 0 || buildTasks.size() > 0;
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
