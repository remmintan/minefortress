package org.minefortress.tasks;

import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.selections.ServerSelectionType;

import java.util.UUID;
import java.util.stream.StreamSupport;

public class BlueprintDigTask extends SimpleSelectionTask {
    public BlueprintDigTask(UUID id, BlockPos startingBlock, BlockPos endingBlock) {
        super(id, TaskType.REMOVE, startingBlock, endingBlock, null, ServerSelectionType.SQUARES, StreamSupport.stream(BlockPos.iterate(startingBlock, endingBlock).spliterator(), false).map(BlockPos::toImmutable).toList());
    }

}
