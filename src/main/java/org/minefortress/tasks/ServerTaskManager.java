package org.minefortress.tasks;


import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;
import net.remmintan.mods.minefortress.core.interfaces.server.ITickableManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IInstantTask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IServerTaskManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.S2CAddClientTasksPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ServerTaskManager implements IServerTaskManager, IWritableManager, ITickableManager {
    private final Map<UUID, ITask> nonFinishedTasks = new HashMap<>();
    private final Queue<ITask> notStartedTasks = new LinkedList<>();

    private static @NotNull List<IWorkerPawn> filterWorkers(List<Integer> selectedPawnIds, ServerPlayerEntity player) {
        final var serverWorld = player.getWorld();
        return selectedPawnIds
                .stream()
                .map(serverWorld::getEntityById)
                .filter(IWorkerPawn.class::isInstance)
                .map(IWorkerPawn.class::cast)
                .filter(worker -> !worker.getTaskControl().isDoingEverydayTasks())
                .toList();
    }

    @Override
    public void addTask(ITask task, List<Integer> selectedPawnIds, ServerPlayerEntity player) {
        removeAllFinishedTasks();

        final var packet = new S2CAddClientTasksPacket(task.toTaskInformationDto());
        FortressServerNetworkHelper.send(player, S2CAddClientTasksPacket.CHANNEL, packet);
        nonFinishedTasks.put(task.getId(), task);

        if (selectedPawnIds.isEmpty()) {
            notStartedTasks.add(task);
            return;
        }

        final var selectedWorkers = filterWorkers(selectedPawnIds, player);
        if (selectedWorkers.isEmpty()) {
            player.sendMessage(Text.of("No appropriate workers selected. Put the task in the queue"), false);
            notStartedTasks.add(task);
            return;
        }

        task.prepareTask();
        setPawnsToTask(task, selectedWorkers);
    }

    @Override
    public void tick(@Nullable ServerPlayerEntity player) {
        if (player == null) return;
        if (notStartedTasks.isEmpty()) return;
        final var freeWorkers = CoreModUtils.getFortressManager(player).getFreeWorkers();
        if (freeWorkers.size() > 2) {
            final var task = notStartedTasks.remove();
            final var freeWorkersIds = freeWorkers.stream().map(it -> ((Entity) it).getId()).toList();
            this.addTask(task, freeWorkersIds, player);
        }
    }

    @Override
    public void executeInstantTask(IInstantTask task, ServerPlayerEntity player) {
        task.execute(player.getServerWorld(), player, getManagersProvider(player)::getBuildingsManager);
    }

    @Override
    public void cancelTask(UUID id, ServerPlayerEntity player) {
        removeAllFinishedTasks();
        final var removedTask = nonFinishedTasks.remove(id);
        if(removedTask != null)
            removedTask.cancel();
        getManagersProvider(player).getResourceManager().returnReservedItems(id);
    }

    private void removeAllFinishedTasks() {
        final var finishedTasks = nonFinishedTasks.entrySet()
                .stream()
                .filter(e -> e.getValue().taskFullyFinished())
                .toList();
        finishedTasks.forEach(e -> nonFinishedTasks.remove(e.getKey()));
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
