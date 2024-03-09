package net.remmintan.mods.minefortress.core.interfaces.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.List;

public interface ITaskPart {
    Pair<BlockPos, BlockPos> getStartAndEnd();

    Iterator<ITaskBlockInfo> getIterator();

    List<ITaskBlockInfo> getBlocks();

    ITask getTask();

    default void returnTaskPart() {
        getTask().returnPart(this.getStartAndEnd());
    }
}
