package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo;
import org.minefortress.tasks.block.info.TaskBlockInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlueprintTask extends AbstractTask {

    private final Map<BlockPos, BlockState> blueprintData;

    public BlueprintTask(UUID id, BlockPos startingPos, BlockPos endingPos, Map<BlockPos, BlockState> blueprintData) {
        super(id, TaskType.BUILD, startingPos, endingPos);
        this.blueprintData = blueprintData;
    }

    @Override
    public TaskPart getNextPart(ServerWorld level) {
        final Pair<BlockPos, BlockPos> partStartAndEnd = parts.poll();
        final Iterable<BlockPos> allPositionsInPart = BlockPos.iterate(partStartAndEnd.getFirst(), partStartAndEnd.getSecond());

        List<TaskBlockInfo> blockInfos = new ArrayList<>();
        for (BlockPos pos : allPositionsInPart) {
            final BlockState state = blueprintData.getOrDefault(pos, Blocks.AIR.getDefaultState());
            final BlockStateTaskBlockInfo blockStateTaskBlockInfo = new BlockStateTaskBlockInfo(getItemFromState(state), pos.toImmutable(), state);
            blockInfos.add(blockStateTaskBlockInfo);
        }
        return new TaskPart(partStartAndEnd, blockInfos, this);
    }

    private Item getItemFromState(BlockState state) {
        final Block block = state.getBlock();
        return block.asItem();
    }

}
