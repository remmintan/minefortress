package org.minefortress.tasks;


import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IServerTaskManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.S2CAddClientTasksPacket;
import org.minefortress.fortress.resources.ItemInfo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ServerTaskManager implements IServerTaskManager, IWritableManager {
    private final Map<UUID, ITask> nonFinishedTasks = new HashMap<>();

    @Override
    public void addTask(ITask task, IServerManagersProvider provider, IServerFortressManager manager, List<Integer> selectedPawns, ServerPlayerEntity player) {
        removeAllFinishedTasks();
        task.prepareTask();
        if(task.hasAvailableParts()) {
            if(task instanceof SimpleSelectionTask simpleSelectionTask) {
                if(manager.isSurvival() && task.getTaskType() == TaskType.BUILD) {
                    final var spliterator = simpleSelectionTask
                            .getBlocksForPart(Pair.of(simpleSelectionTask.getStartingBlock(), simpleSelectionTask.getEndingBlock()))
                            .spliterator();

                    final var blocksCount = (int)StreamSupport.stream(spliterator, false).count();
                    final var placingItem = simpleSelectionTask.getPlacingItem();

                    final var info = new ItemInfo(placingItem, blocksCount);

                    provider.getResourceManager().reserveItems(task.getId(), Collections.singletonList(info));
                }
            }
        }

        final var serverWorld = player.getWorld();
        final var selectedWorkers = selectedPawns
                .stream()
                .map(serverWorld::getEntityById)
                .filter(IWorkerPawn.class::isInstance)
                .map(IWorkerPawn.class::cast)
                .toList();

        final var assignmentResult = assignPawnsToTask(player, task, selectedWorkers);

        if(assignmentResult) {
            final var packet = new S2CAddClientTasksPacket(task.toTaskInformationDto());
            FortressServerNetworkHelper.send(player, S2CAddClientTasksPacket.CHANNEL, packet);
            nonFinishedTasks.put(task.getId(), task);
        }
    }

    @Override
    public void cancelTask(UUID id, IServerManagersProvider provider, IServerFortressManager manager) {
        removeAllFinishedTasks();
        final var removedTask = nonFinishedTasks.remove(id);
        if(removedTask != null)
            removedTask.cancel();
        provider.getResourceManager().returnReservedItems(id);
    }

    private void removeAllFinishedTasks() {
        final var finishedTasks = nonFinishedTasks.entrySet()
                .stream()
                .filter(e -> e.getValue().taskFullyFinished())
                .toList();
        finishedTasks.forEach(e -> nonFinishedTasks.remove(e.getKey()));
    }

    private boolean assignPawnsToTask(ServerPlayerEntity player, ITask task, List<IWorkerPawn> workers) {
        if(workers.isEmpty()) {
            player.sendMessage(Text.of("No workers selected"), false);
            return false;
        }
        final TaskType taskType = task.getTaskType();
        if(taskType == TaskType.BUILD) {
            setPawnsToTask(task, workers);
        } else {
            final List<String> professions = getProfessionIdFromTask(task);
            final List<IWorkerPawn> professionals = workers
                    .stream()
                    .filter(c -> professions.contains(c.getProfessionId()))
                    .collect(Collectors.toList());
            if(professionals.isEmpty()) {
                player.sendMessage(Text.of("No appropriate professionals selected"), false);
                return false;
            }
            setPawnsToTask(task, professionals);
        }
        return true;
    }

    private List<String> getProfessionIdFromTask(ITask task) {
        if(task instanceof CutTreesTask) {
            return Arrays.asList("lumberjack1", "lumberjack2", "lumberjack3", "colonist");
        }
        return List.of("colonist");
    }

    private void setPawnsToTask(ITask task, List<IWorkerPawn> workers) {
        for(IWorkerPawn worker : workers) {
            if(!task.hasAvailableParts()) break;
            worker.getTaskControl().setTask(task);
        }
    }


    @Override
    public void write(NbtCompound tag) {

    }

    @Override
    public void read(NbtCompound tag) {

    }
}
