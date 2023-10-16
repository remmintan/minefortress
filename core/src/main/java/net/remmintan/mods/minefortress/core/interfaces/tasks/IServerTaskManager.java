package net.remmintan.mods.minefortress.core.interfaces.tasks;

import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.selections.ServerSelectionType;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;

import java.util.List;
import java.util.UUID;

public interface IServerTaskManager {
    void tick(IServerFortressManager manager, ServerWorld world);
    void addTask(ITask task, IServerManagersProvider provider, IServerFortressManager manager);
    void cancelTask(UUID id, IServerManagersProvider provider, IServerFortressManager manager);
    boolean hasTask();
    ITask createCutTreesTask(UUID uuid, List<BlockPos> treeRoots);
    default ITask createRoadsTask(UUID digUuid, TaskType type, UUID placeUuid, List<BlockPos> blocks, Item itemInHand) {
        return createRoadsTask(digUuid, type, placeUuid, blocks, itemInHand, () -> {});
    }
    ITask createRoadsTask(UUID digUuid, TaskType type, UUID placeUuid, List<BlockPos> blocks, Item itemInHand, Runnable onComplete);

    ITask createSelectionTask(UUID id, TaskType taskType, BlockPos start, BlockPos end, ServerSelectionType selectionType, HitResult hitResult, ServerPlayerEntity player);
}
