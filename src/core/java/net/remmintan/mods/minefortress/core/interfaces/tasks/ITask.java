package net.remmintan.mods.minefortress.core.interfaces.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;

import java.util.UUID;

public interface ITask {
    UUID getId();
    TaskType getTaskType();
    default void prepareTask() {}
    boolean hasAvailableParts();
    ITaskPart getNextPart(ServerWorld level, IWorkerPawn colonist);
    void returnPart(Pair<BlockPos, BlockPos> partStartAndEnd);
    void finishPart(ITaskPart part, IWorkerPawn colonist);

    default void addFinishListener(Runnable listener) {}

}
