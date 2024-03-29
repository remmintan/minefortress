package net.remmintan.mods.minefortress.core.interfaces.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;

import java.util.List;
import java.util.UUID;

public interface ITask {
    UUID getId();
    TaskType getTaskType();
    default void prepareTask() {}
    boolean hasAvailableParts();
    ITaskPart getNextPart(IWorkerPawn colonist);
    void returnPart(Pair<BlockPos, BlockPos> partStartAndEnd);
    void finishPart(ITaskPart part, IWorkerPawn colonist);

    default void addFinishListener(Runnable listener) {}
    List<TaskInformationDto> toTaskInformationDto();

    boolean taskFullyFinished();
    void cancel();
    boolean isCanceled();

}
