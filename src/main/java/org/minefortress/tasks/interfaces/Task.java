package org.minefortress.tasks.interfaces;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.tasks.TaskPart;
import org.minefortress.tasks.TaskType;

import java.util.UUID;

public interface Task {

    UUID getId();
    TaskType getTaskType();
    default void prepareTask() {}

    boolean hasAvailableParts();
    TaskPart getNextPart(ServerWorld level);
    void returnPart(Pair<BlockPos, BlockPos> partStartAndEnd);
    void finishPart(TaskPart part, Colonist colonist);

}
