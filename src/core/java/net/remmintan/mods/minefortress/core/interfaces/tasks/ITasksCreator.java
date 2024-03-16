package net.remmintan.mods.minefortress.core.interfaces.tasks;

import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.selections.ServerSelectionType;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;

import java.util.List;
import java.util.UUID;

public interface ITasksCreator extends IServerManager {

    ITask createCutTreesTask(UUID uuid, List<BlockPos> treeRoots, List<BlockPos> positions);
    default ITask createRoadsTask(UUID digUuid, TaskType type, UUID placeUuid, List<BlockPos> blocks, Item itemInHand) {
        return createRoadsTask(digUuid, type, placeUuid, blocks, itemInHand, () -> {});
    }
    ITask createRoadsTask(UUID digUuid, TaskType type, UUID placeUuid, List<BlockPos> blocks, Item itemInHand, Runnable onComplete);

    ITask createSelectionTask(UUID id, TaskType taskType, BlockPos start, BlockPos end, ServerSelectionType selectionType, HitResult hitResult, List<BlockPos> positions, ServerPlayerEntity player);

}
