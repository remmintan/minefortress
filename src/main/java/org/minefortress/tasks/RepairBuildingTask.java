package org.minefortress.tasks;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart;
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RepairBuildingTask extends AbstractTask {

    private final Map<BlockPos, BlockState> blocksToRepair;

    public RepairBuildingTask(UUID id, BlockPos startingBlock, BlockPos endingBlock, Map<BlockPos, BlockState> blocksToRepair) {
        super(id, TaskType.BUILD, startingBlock, endingBlock);
        this.blocksToRepair = Collections.unmodifiableMap(blocksToRepair);
    }

    @Override
    public ITaskPart getNextPart(IWorkerPawn colonist) {
        final var part = parts.remove();
        final var taskBlocks = BlockPos.stream(part.getFirst(), part.getSecond())
                .map(BlockPos::toImmutable)
                .filter(blocksToRepair::containsKey)
                .map(it -> {
                    final var state = blocksToRepair.get(it);
                    final var item = Item.BLOCK_ITEMS.get(state.getBlock());
                    return new BlockStateTaskBlockInfo(item, it, state);
                })
                .map(ITaskBlockInfo.class::cast)
                .toList();

        return new TaskPart(part, taskBlocks, this);
    }

    @Override
    public List<TaskInformationDto> toTaskInformationDto() {
        final var taskInfoDto = new TaskInformationDto(id, blocksToRepair.keySet().stream().toList(), taskType);
        return List.of(taskInfoDto);
    }
}
