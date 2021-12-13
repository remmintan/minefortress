package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;

public class TaskPart {

    private final Pair<BlockPos, BlockPos> startAndEnd;
    private final Iterator<BlockPos> iterator;
    private final Task task;


    public TaskPart(final Pair<BlockPos, BlockPos> startAndEnd, final Iterator<BlockPos> iterator, Task task) {
        this.startAndEnd = startAndEnd;
        this.iterator = iterator;
        this.task = task;
    }

    public Pair<BlockPos, BlockPos> getStartAndEnd() {
        return startAndEnd;
    }

    public Iterator<BlockPos> getIterator() {
        return iterator;
    }

    public Task getTask() {
        return task;
    }
}
