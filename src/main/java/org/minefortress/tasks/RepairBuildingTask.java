package org.minefortress.tasks;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart;
import org.jetbrains.annotations.NotNull;
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RepairBuildingTask extends AbstractTask {

    private final Map<BlockPos, BlockState> blocksToRepair;
    private final List<ItemStack> repairItems;

    public RepairBuildingTask(BlockPos startingBlock, BlockPos endingBlock, Map<BlockPos, BlockState> blocksToRepair, List<ItemStack> repairItems) {
        super(TaskType.BUILD, startingBlock, endingBlock);
        this.blocksToRepair = Collections.unmodifiableMap(blocksToRepair);
        this.repairItems = repairItems;
    }

    @Override
    public @NotNull List<BlockPos> getPositions() {
        return blocksToRepair.keySet().stream().toList();
    }

    public List<ItemStack> getRepairItems() {
        return repairItems;
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
    @NotNull
    public List<TaskInformationDto> toTaskInformationDto() {
        final var taskInfoDto = new TaskInformationDto(getPos(), getPositions(), taskType);
        return List.of(taskInfoDto);
    }
}
