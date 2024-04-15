package net.remmintan.mods.minefortress.core.interfaces.tasks;

import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;

import java.util.List;
import java.util.UUID;

public interface IServerTaskManager extends IServerManager {
    boolean addTask(ITask task, IServerManagersProvider provider, IServerFortressManager manager, List<Integer> selectedPawns, ServerPlayerEntity player);
    void cancelTask(UUID id, IServerManagersProvider provider, IServerFortressManager manager);
}
