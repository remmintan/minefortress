package net.remmintan.mods.minefortress.core.interfaces.tasks;

import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;

public interface IClientTasksHolder {

    void addRoadsSelectionTask(UUID digTaskId, UUID placeTaskId, List<BlockPos> positions);
    void addTask(UUID uuid, Iterable<BlockPos> blocks);

}
