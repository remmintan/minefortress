package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.BlockPos;
import org.minefortress.tasks.block.info.TaskBlockInfo;
import org.minefortress.tasks.interfaces.Task;

import java.util.Iterator;
import java.util.List;

public class TaskPart {

    private final Pair<BlockPos, BlockPos> startAndEnd;
    private final List<TaskBlockInfo> blocks;
    private final Task task;


    public TaskPart(final Pair<BlockPos, BlockPos> startAndEnd, List<TaskBlockInfo> blocks, Task task) {
        this.startAndEnd = startAndEnd;
        this.blocks = blocks;
        this.task = task;
    }

    public Pair<BlockPos, BlockPos> getStartAndEnd() {
        return startAndEnd;
    }

    public Iterator<TaskBlockInfo> getIterator() {
        return blocks.iterator();
    }

    public List<TaskBlockInfo> getBlocks() {
        return blocks;
    }

    public Task getTask() {
        return task;
    }
}
