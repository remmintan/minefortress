package org.minefortress.tasks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.math.BlockPos;
import org.minefortress.network.ServerboundCancelTaskPacket;
import org.minefortress.selections.ClientSelection;
import org.minefortress.selections.SelectionType;
import org.minefortress.tasks.TaskType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClientTasksHolder {

    private final Map<UUID, UUID> subtasksMap = new HashMap<>();

    private final Map<UUID, ClientSelection> removeTasks = new HashMap<>();
    private final Map<UUID, ClientSelection> buildTasks = new HashMap<>();

    private final Stack<UUID> tasksStack = new Stack<>();

    private final ClientWorld level;
    private final WorldRenderer levelRenderer;

    public ClientTasksHolder(ClientWorld level, WorldRenderer levelRenderer) {
        this.level = level;
        this.levelRenderer = levelRenderer;
    }


    public void cancelTask() {
        if(tasksStack.empty()) return;
        final UUID lastTaskId = tasksStack.pop();
        final ClientConnection connection = MinecraftClient.getInstance().getNetworkHandler().getConnection();
        subtasksMap.entrySet().stream().filter(it -> it.getValue().equals(lastTaskId)).map(Map.Entry::getKey).forEach(it -> {
            removeTask(it);
            if (connection != null) {
                connection.send(new ServerboundCancelTaskPacket(lastTaskId));
            }
        });
        removeTask(lastTaskId);

        if (connection != null) {
            connection.send(new ServerboundCancelTaskPacket(lastTaskId));
        }
    }

    public void cancelAllTasks() {
        while(!tasksStack.empty()) {
            cancelTask();
        }
    }

    public void addTask(UUID uuid, SelectionType selectionType, Iterable<BlockPos> blocks, BlockState blockState, TaskType type) {
        addTask(uuid, selectionType, blocks, blockState, type, null);
    }

    public void addTask(UUID uuid, SelectionType selectionType, Iterable<BlockPos> blocks, BlockState blockState, TaskType type, UUID superTaskId) {
        if(blockState == null) {
            blockState = Blocks.DIRT.getDefaultState();
        }
        ClientSelection newTask = new ClientSelection(uuid, selectionType, blocks, blockState);
        if(superTaskId != null) {
            subtasksMap.put(uuid, superTaskId);
        }

        if(type == TaskType.REMOVE) {
            removeTasks.put(uuid, newTask);
        } else {
            buildTasks.put(uuid, newTask);
            updateRenderer(blocks, blockState);
            compileBlocksToRender();
        }

        tasksStack.push(uuid);
    }

    private void updateRenderer(Iterable<BlockPos> blocks, BlockState blockState) {
        levelRenderer.scheduleTerrainUpdate();
        blocks.forEach(it -> levelRenderer.scheduleBlockRerenderIfNeeded(it, level.getBlockState(it), blockState));
    }

    public Collection<ClientSelection> getAllRemoveTasks() {
        return new HashSet<>(removeTasks.values());
    }

    private final Map<BlockPos, BlockState> blocksToRender = new ConcurrentHashMap<>();
    private void compileBlocksToRender() {
        blocksToRender.clear();
        for(ClientSelection selection : buildTasks.values()) {
            BlockState blockState = selection.getBuildingBlockState();
            for(BlockPos pos: selection.getBlockPositions()) {
                blocksToRender.put(pos.toImmutable(), blockState);
            }
        }
    }

    public Map<BlockPos, BlockState> getAllBuildTasks() {
        return blocksToRender;
    }

    public void removeTask(UUID uuid) {
        if(buildTasks.containsKey(uuid)) {
            ClientSelection clientSelection = buildTasks.get(uuid);
            updateRenderer(clientSelection.getBlockPositions(), clientSelection.getBuildingBlockState());
            buildTasks.remove(uuid);
            compileBlocksToRender();
        } else {
            removeTasks.remove(uuid);
        }

        subtasksMap.remove(uuid);
        tasksStack.remove(uuid);
    }

}
