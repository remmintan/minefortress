package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart;

import java.util.Iterator;
import java.util.List;

public class TaskPart implements ITaskPart {

    private final Pair<BlockPos, BlockPos> startAndEnd;

    private final Iterator<ITaskBlockInfo> iterator;
    private final ITask task;


    public TaskPart(final Pair<BlockPos, BlockPos> startAndEnd, List<ITaskBlockInfo> blocks, ITask task) {
        this.startAndEnd = startAndEnd;
        this.task = task;
        this.iterator = blocks.iterator();
    }

    @Override
    public Pair<BlockPos, BlockPos> getStartAndEnd() {
        return startAndEnd;
    }

    @Override
    public ITask getTask() {
        return task;
    }


    @Override
    public boolean hasNext() {
        return !task.isCanceled() && iterator.hasNext();
    }

    @Override
    public ITaskBlockInfo next() {
        return iterator.next();
    }
}
