package org.minefortress.tasks;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.selections.ServerSelectionType;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksCreator;

import java.util.List;
import java.util.UUID;

public class TasksCreator implements ITasksCreator {

    @Override
    public ITask createCutTreesTask(UUID uuid, List<BlockPos> treeRoots, List<BlockPos> positions) {
        return new CutTreesTask(uuid, treeRoots, positions);
    }

    @Override
    public ITask createRoadsTask(UUID digUuid, TaskType type, UUID placeUuid, List<BlockPos> blocks, Item itemInHand, Runnable onComplete) {
        return new RoadsTask(placeUuid, type, blocks, itemInHand, onComplete);
    }

    @Override
    public ITask createSelectionTask(UUID id, TaskType taskType, BlockPos start, BlockPos end, ServerSelectionType selectionType, HitResult hitResult, List<BlockPos> positions, ServerPlayerEntity player) {
        SimpleSelectionTask simpleSelectionTask = new SimpleSelectionTask(id, taskType, start, end, hitResult, selectionType, positions);
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


}
