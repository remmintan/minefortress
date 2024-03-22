package net.remmintan.mods.minefortress.core.interfaces.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;

public interface ITaskPart  extends  Iterator<ITaskBlockInfo> {
    Pair<BlockPos, BlockPos> getStartAndEnd();

    ITask getTask();

    default void returnTaskPart() {
        getTask().returnPart(this.getStartAndEnd());
    }
}
