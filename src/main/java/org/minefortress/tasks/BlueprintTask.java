package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo;
import org.minefortress.tasks.block.info.TaskBlockInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlueprintTask extends AbstractTask {

    private final Map<BlockPos, BlockState> blueprintData;
    private final Map<BlockPos, BlockState> blueprintEntityData;

    public BlueprintTask(UUID id, BlockPos startingPos, BlockPos endingPos, Map<BlockPos, BlockState> blueprintData, Map<BlockPos, BlockState> blueprintEntityData) {
        super(id, TaskType.BUILD, startingPos, endingPos);
        this.blueprintData = blueprintData;
        this.blueprintEntityData = blueprintEntityData;
    }

    @Override
    public TaskPart getNextPart(ServerWorld level) {
        final Pair<BlockPos, BlockPos> partStartAndEnd = parts.poll();
        List<TaskBlockInfo> blockInfos = getTaskBlockInfos(partStartAndEnd);
        return new TaskPart(partStartAndEnd, blockInfos, this);
    }

    @NotNull
    private List<TaskBlockInfo> getTaskBlockInfos(Pair<BlockPos, BlockPos> partStartAndEnd) {
        final BlockPos start = partStartAndEnd.getFirst();
        final BlockPos delta = start.subtract(startingBlock);
        final Iterable<BlockPos> allPositionsInPart = BlockPos.iterate(start, partStartAndEnd.getSecond());

        List<TaskBlockInfo> blockInfos = new ArrayList<>();
        for (BlockPos pos : allPositionsInPart) {
            final BlockState state = blueprintData.getOrDefault(pos.subtract(start).add(delta), Blocks.AIR.getDefaultState());
            if(state.isAir()) continue;
            final BlockStateTaskBlockInfo blockStateTaskBlockInfo = new BlockStateTaskBlockInfo(getItemFromState(state), pos.toImmutable(), state);
            blockInfos.add(blockStateTaskBlockInfo);
        }
        return blockInfos;
    }

    @Override
    public void finishPart(ServerWorld world) {
        if(parts.isEmpty() && getCompletedParts()+1 == totalParts) {
            final ServerPlayerEntity randomAlivePlayer = world.getRandomAlivePlayer();
            blueprintEntityData.forEach((pos, state) -> {
                world.setBlockState(pos.add(startingBlock), state, 3);
//                world.emitGameEvent(randomAlivePlayer, GameEvent.BLOCK_PLACE, pos);
            });

        }
        super.finishPart(world);
    }

    private Item getItemFromState(BlockState state) {
        final Block block = state.getBlock();
        return block.asItem();
    }

}
