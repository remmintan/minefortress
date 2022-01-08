package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.BlockPos;
import org.minefortress.tasks.block.info.TaskBlockInfo;
import org.minefortress.tasks.interfaces.Task;

import java.util.Iterator;
import java.util.List;

public class TaskPart {

    private final Pair<BlockPos, BlockPos> startAndEnd;
    private final List<TaskBlockInfo> iterator;
    private final Task task;


    public TaskPart(final Pair<BlockPos, BlockPos> startAndEnd, List<TaskBlockInfo> iterator, Task task) {
        this.startAndEnd = startAndEnd;
        this.iterator = iterator;
        this.task = task;
    }

    public Pair<BlockPos, BlockPos> getStartAndEnd() {
        return startAndEnd;
    }

    public Iterator<TaskBlockInfo> getIterator() {
        return iterator.iterator();
    }

    public Task getTask() {
        return task;
    }
}
