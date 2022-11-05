package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.IWorkerPawn;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.tasks.interfaces.Task;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TaskManager {

    private final static Set<String> BUILDER_PROFESSIONS = Set.of("miner1", "miner2", "miner3", "lumberjack1", "lumberjack2", "lumberjack3", "colonist");

    private final Deque<Task> tasks = new ArrayDeque<>();
    private final Set<UUID> cancelledTasks = new HashSet<>();

    public void tick(FortressServerManager manager, ServerWorld world) {
        if(!hasTask()) return;
        final Task task = this.getTask();
        final List<IWorkerPawn> freeColonists = manager.getFreeColonists();
        if(freeColonists.isEmpty()) return;
        final TaskType taskType = task.getTaskType();
        if(taskType == TaskType.BUILD) {
            final List<IWorkerPawn> completelyFreePawns = getCompletelyFreePawns(task, freeColonists);

            boolean fullyCompleted = setPawnsToTask(world, task, completelyFreePawns);
            if(fullyCompleted) return;

            final List<IWorkerPawn> otherPawns = freeColonists
                    .stream()
                    .filter(c -> isBuilderProfession(c.getProfessionId()))
                    .filter(c -> c.getTaskControl().canStartTask(task))
                    .filter(c -> c.getTaskControl().isDoingEverydayTasks())

                    .collect(Collectors.toList());
            setPawnsToTask(world, task, otherPawns);
        } else {
            final List<String> professions = getProfessionIdFromTask(task);
            final List<IWorkerPawn> professionals = freeColonists
                    .stream()
                    .filter(c -> professions.contains(c.getProfessionId()))
                    .filter(c -> c.getTaskControl().canStartTask(task))
                    .collect(Collectors.toList());

            boolean fullyCompleted = setPawnsToTask(world, task, professionals);
            if(fullyCompleted) return;

            final List<IWorkerPawn> completelyFreePawns = getCompletelyFreePawns(task, freeColonists);
            setPawnsToTask(world, task, completelyFreePawns);
        }
    }

    public void addTask(Task task, FortressServerManager manager) {
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

                    manager.getServerResourceManager().reserveItems(task.getId(), Collections.singletonList(info));
                }
            }
            tasks.add(task);
        }
    }

    public void cancelTask(UUID id, FortressServerManager manager) {
        cancelledTasks.add(id);
        manager.getServerResourceManager().returnReservedItems(id);
        tasks.removeIf(task -> task.getId().equals(id));
    }

    @NotNull
    private List<IWorkerPawn> getCompletelyFreePawns(Task task, List<IWorkerPawn> freeColonists) {
        return freeColonists
                .stream()
                .filter(c -> isBuilderProfession(c.getProfessionId()))
                .filter(c -> c.getTaskControl().canStartTask(task))
                .filter(c -> !c.getTaskControl().isDoingEverydayTasks())
                .collect(Collectors.toList());
    }

    private List<String> getProfessionIdFromTask(Task task) {
        if(task instanceof CutTreesTask) {
            return Arrays.asList("lumberjack1", "lumberjack2", "lumberjack3");
        }
        return Arrays.asList("miner1", "miner2", "miner3");
    }

    private static boolean isBuilderProfession(String professionId) {
        return BUILDER_PROFESSIONS.contains(professionId);
    }

    private boolean setPawnsToTask(ServerWorld world, Task task, List<IWorkerPawn> completelyFreePawns) {
        for(IWorkerPawn c : completelyFreePawns) {
            if(!task.hasAvailableParts()) break;
            c.getTaskControl().setTask(task, task.getNextPart(world, c), this::returnTaskPart, () -> this.isCancelled(task.getId()));
        }
        if(!task.hasAvailableParts()) {
            tasks.remove();
            return true;
        }
        return false;
    }

    public boolean hasTask() {
        return !tasks.isEmpty();
    }

    private Task getTask() {
        return tasks.element();
    }

    private void returnTaskPart(TaskPart taskPart) {
        Task task = taskPart.getTask();
        task.returnPart(taskPart.getStartAndEnd());

        if(!tasks.contains(task)) {
            tasks.addFirst(task);
        }
    }

    private boolean isCancelled(UUID id) {
        return cancelledTasks.contains(id);
    }

}
