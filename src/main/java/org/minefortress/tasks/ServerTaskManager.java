package org.minefortress.tasks;


import com.mojang.datafixers.util.Pair;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IServerTaskManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;
import org.minefortress.fortress.resources.ItemInfo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ServerTaskManager implements IServerTaskManager {
    private final Set<UUID> cancelledTasks = new HashSet<>();

    @Override
    public void addTask(ITask task, IServerManagersProvider provider, IServerFortressManager manager, List<Integer> selectedPawns, ServerPlayerEntity player) {
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

        assignPawnsToTask(player, task, selectedWorkers);
    }

    @Override
    public void cancelTask(UUID id, IServerManagersProvider provider, IServerFortressManager manager) {
        cancelledTasks.add(id);
        provider.getResourceManager().returnReservedItems(id);
    }

    private void assignPawnsToTask(ServerPlayerEntity player, ITask task, List<IWorkerPawn> workers) {
        if(workers.isEmpty()) {
            player.sendMessage(Text.of("No workers selected"), false);
            return;
        }
        final TaskType taskType = task.getTaskType();
        if(taskType == TaskType.BUILD) {
            setPawnsToTask(player.getServerWorld(), task, workers);
        } else {
            final List<String> professions = getProfessionIdFromTask(task);
            final List<IWorkerPawn> professionals = workers
                    .stream()
                    .filter(c -> professions.contains(c.getProfessionId()))
                    .filter(c -> c.getTaskControl().canStartTask(task))
                    .collect(Collectors.toList());
            if(professionals.isEmpty()) {
                player.sendMessage(Text.of("No appropriate professionals selected"), false);
                return;
            }
            setPawnsToTask(player.getServerWorld(), task, professionals);
        }
    }

    private List<String> getProfessionIdFromTask(ITask task) {
        if(task instanceof CutTreesTask) {
            return Arrays.asList("lumberjack1", "lumberjack2", "lumberjack3");
        }
        return List.of("colonist");
    }

    private void setPawnsToTask(ServerWorld world, ITask task, List<IWorkerPawn> workers) {
        for(IWorkerPawn worker : workers) {
            if(!task.hasAvailableParts()) break;
            worker.getTaskControl().setTask(task, task.getNextPart(world, worker), () -> cancelledTasks.contains(task.getId()));
        }
    }


}
