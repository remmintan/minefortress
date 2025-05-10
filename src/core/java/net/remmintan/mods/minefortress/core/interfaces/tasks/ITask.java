package net.remmintan.mods.minefortress.core.interfaces.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;

public interface ITask extends IBaseTask {

    TaskType getTaskType();
    boolean hasAvailableParts();
    ITaskPart getNextPart(IWorkerPawn colonist);
    void returnPart(Pair<BlockPos, BlockPos> partStartAndEnd);
    void finishPart(ITaskPart part, IWorkerPawn colonist);

    default void addFinishListener(Runnable listener) {}

}
