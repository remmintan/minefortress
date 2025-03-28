package net.remmintan.mods.minefortress.core.interfaces.tasks;

import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;

import java.util.List;
import java.util.UUID;

public interface IServerTaskManager extends IServerManager {
    void addTask(ITask task, List<Integer> selectedPawnIds, ServerPlayerEntity player);


    void cancelTask(UUID id, ServerPlayerEntity player);
}
