package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart;

import java.util.Iterator;
import java.util.List;

public class TaskPart implements ITaskPart {

    private final Pair<BlockPos, BlockPos> startAndEnd;
    private final List<ITaskBlockInfo> blocks;
    private final ITask task;


    public TaskPart(final Pair<BlockPos, BlockPos> startAndEnd, List<ITaskBlockInfo> blocks, ITask task) {
        this.startAndEnd = startAndEnd;
        this.blocks = blocks;
        this.task = task;
    }

    @Override
    public Pair<BlockPos, BlockPos> getStartAndEnd() {
        return startAndEnd;
    }

    @Override
    public Iterator<ITaskBlockInfo> getIterator() {
        return blocks.iterator();
    }

    @Override
    public List<ITaskBlockInfo> getBlocks() {
        return blocks;
    }

    @Override
    public ITask getTask() {
        return task;
    }
}
