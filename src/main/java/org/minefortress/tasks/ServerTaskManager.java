package org.minefortress.tasks;


import com.mojang.datafixers.util.Pair;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;
import net.remmintan.mods.minefortress.core.interfaces.selections.ServerSelectionType;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IServerTaskManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart;
import org.jetbrains.annotations.NotNull;
import org.minefortress.fortress.resources.ItemInfo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ServerTaskManager implements IServerTaskManager {

    private final static Set<String> BUILDER_PROFESSIONS = Set.of("miner1", "miner2", "miner3", "colonist");

    private final Deque<ITask> tasks = new ArrayDeque<>();
    private final Set<UUID> cancelledTasks = new HashSet<>();

    @Override
    public void tick(IServerFortressManager manager, ServerWorld world) {
        if(!hasTask()) return;
        final ITask task = this.getTask();
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

    @Override
    public void addTask(ITask task, IServerManagersProvider provider, IServerFortressManager manager) {
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
            tasks.add(task);
        }
    }

    @Override
    public void cancelTask(UUID id, IServerManagersProvider provider, IServerFortressManager manager) {
        cancelledTasks.add(id);
        provider.getResourceManager().returnReservedItems(id);
        tasks.removeIf(task -> task.getId().equals(id));
    }

    @NotNull
    private List<IWorkerPawn> getCompletelyFreePawns(ITask task, List<IWorkerPawn> freeColonists) {
        return freeColonists
                .stream()
                .filter(c -> isBuilderProfession(c.getProfessionId()))
                .filter(c -> c.getTaskControl().canStartTask(task))
                .filter(c -> !c.getTaskControl().isDoingEverydayTasks())
                .collect(Collectors.toList());
    }

    private List<String> getProfessionIdFromTask(ITask task) {
        if(task instanceof CutTreesTask) {
            return Arrays.asList("lumberjack1", "lumberjack2", "lumberjack3");
        }
        return Arrays.asList("miner1", "miner2", "miner3");
    }

    private static boolean isBuilderProfession(String professionId) {
        return BUILDER_PROFESSIONS.contains(professionId);
    }

    private boolean setPawnsToTask(ServerWorld world, ITask task, List<IWorkerPawn> completelyFreePawns) {
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

    @Override
    public boolean hasTask() {
        return !tasks.isEmpty();
    }

    @Override
    public ITask createCutTreesTask(UUID uuid, List<BlockPos> treeRoots) {
        return new CutTreesTask(uuid, treeRoots);
    }

    @Override
    public ITask createRoadsTask(UUID digUuid, TaskType type, UUID placeUuid, List<BlockPos> blocks, Item itemInHand, Runnable onComplete) {
        return new RoadsTask(placeUuid, type, blocks, itemInHand, onComplete);
    }

    @Override
    public ITask createSelectionTask(UUID id, TaskType taskType, BlockPos start, BlockPos end, ServerSelectionType selectionType, HitResult hitResult, ServerPlayerEntity player) {
        SimpleSelectionTask simpleSelectionTask = new SimpleSelectionTask(id, taskType, start, end, hitResult, selectionType);
        if(simpleSelectionTask.getTaskType() == TaskType.BUILD) {
            final ItemStack itemInHand = player.getStackInHand(Hand.MAIN_HAND);
            if(itemInHand != ItemStack.EMPTY) {
                simpleSelectionTask.setPlacingItem(itemInHand.getItem());
            } else {
                throw new IllegalStateException();
            }
        }
        return simpleSelectionTask;
    }

    private ITask getTask() {
        return tasks.element();
    }

    private void returnTaskPart(ITaskPart taskPart) {
        ITask task = taskPart.getTask();
        task.returnPart(taskPart.getStartAndEnd());

        if(!tasks.contains(task)) {
            tasks.addFirst(task);
        }
    }

    private boolean isCancelled(UUID id) {
        return cancelledTasks.contains(id);
    }

}
