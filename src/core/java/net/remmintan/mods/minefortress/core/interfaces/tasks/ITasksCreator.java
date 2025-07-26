package net.remmintan.mods.minefortress.core.interfaces.tasks;

import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.selections.ServerSelectionType;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;

import java.util.List;

public interface ITasksCreator extends IServerManager {

    ITask createCutTreesTask(List<BlockPos> treeRoots);

    ITask createRoadsTask(List<BlockPos> blocks, Item itemInHand);

    ITask createSelectionTask(TaskType taskType, BlockPos start, BlockPos end, ServerSelectionType selectionType, HitResult hitResult, List<BlockPos> positions, ServerPlayerEntity player);

}
