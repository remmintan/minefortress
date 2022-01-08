package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.minefortress.network.ClientboundTaskExecutedPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.selections.SelectionType;
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo;
import org.minefortress.tasks.block.info.DigTaskBlockInfo;
import org.minefortress.tasks.block.info.ItemTaskBlockInfo;
import org.minefortress.tasks.block.info.TaskBlockInfo;
import org.minefortress.utils.BlockInfoUtils;
import org.minefortress.utils.PathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SimpleSelectionTask extends AbstractTask {

    private final HitResult hitResult;
    private final Direction horizontalDirection;
    private final SelectionType selectionType;

    private Item placingItem;

    public SimpleSelectionTask(UUID id, TaskType taskType, BlockPos startingBlock, BlockPos endingBlock, HitResult hitResult, SelectionType selectionType) {
        super(id, taskType, startingBlock, endingBlock);
        this.selectionType = selectionType;

        final boolean shouldSwapStartAndEnd = isShouldSwapEnds(taskType, startingBlock, endingBlock, selectionType);
        if(shouldSwapStartAndEnd) {
            this.startingBlock = endingBlock;
            this.endingBlock = startingBlock;
        } else {
            this.startingBlock = startingBlock;
            this.endingBlock = endingBlock;
        }

        if(selectionType == SelectionType.LADDER && hitResult instanceof BlockHitResult) {
            this.horizontalDirection  = startingBlock.getX() > endingBlock.getX() ? Direction.WEST : Direction.EAST;
            this.hitResult = new BlockHitResult(hitResult.getPos(), Direction.UP, ((BlockHitResult) hitResult).getBlockPos(), ((BlockHitResult) hitResult).isInsideBlock());
        } else if (selectionType == SelectionType.LADDER_Z_DIRECTION && hitResult instanceof BlockHitResult) {
            this.horizontalDirection  = startingBlock.getZ() > endingBlock.getZ() ? Direction.NORTH : Direction.SOUTH;
            this.hitResult = new BlockHitResult(hitResult.getPos(), Direction.UP, ((BlockHitResult) hitResult).getBlockPos(), ((BlockHitResult) hitResult).isInsideBlock());
        } else {
            this.hitResult = hitResult;
            this.horizontalDirection = null;
        }
    }

    private boolean isShouldSwapEnds(TaskType taskType, BlockPos startingBlock, BlockPos endingBlock, SelectionType selectionType) {
        boolean removeFromBottomToTop = taskType == TaskType.REMOVE && endingBlock.getY() > startingBlock.getY();
        boolean buildFromTopToBottom = taskType == TaskType.BUILD && endingBlock.getY() < startingBlock.getY();
        boolean isLadder = selectionType == SelectionType.LADDER || selectionType == SelectionType.LADDER_Z_DIRECTION;
        return !isLadder && (removeFromBottomToTop || buildFromTopToBottom);
    }

    @Override
    public void prepareTask() {
        if(selectionType == SelectionType.WALLS_EVERY_SECOND) {
            parts.add(Pair.of(startingBlock, endingBlock));
        } else {
            super.prepareTask();
        }
    }

    public void setPlacingItem(Item placingItem) {
        this.placingItem = placingItem;
    }

    @Override
    public TaskPart getNextPart(ServerWorld world) {
        Pair<BlockPos, BlockPos> startAndEnd = parts.poll();
        if(startAndEnd == null) throw new IllegalStateException("Null part for task!");
        final List<TaskBlockInfo> blocks = getPartBlocksInfo(startAndEnd, world);
        return new TaskPart(startAndEnd, blocks, this);
    }



    private List<TaskBlockInfo> getPartBlocksInfo(Pair<BlockPos, BlockPos> startAndEnd, ServerWorld world) {
        final List<TaskBlockInfo> blocksInfo = new ArrayList<>();
        getBlocksForPart(startAndEnd).spliterator().forEachRemaining(pos -> {
            if(placingItem != null) {
                if(BlockInfoUtils.shouldBePlacedAsItem(placingItem)) {
                    final ItemUsageContext useOnContext = BlockInfoUtils.getUseOnContext(this.hitResult, this.placingItem, pos, world);
                    final ItemTaskBlockInfo itemTaskBlockInfo = new ItemTaskBlockInfo(placingItem, pos, useOnContext);
                    blocksInfo.add(itemTaskBlockInfo);
                } else {
                    final BlockState blockStateForPlacement = BlockInfoUtils.getBlockStateForPlacement(placingItem, hitResult, horizontalDirection, world, pos);
                    final BlockStateTaskBlockInfo blockStateTaskBlockInfo = new BlockStateTaskBlockInfo(placingItem, pos, blockStateForPlacement);
                    blocksInfo.add(blockStateTaskBlockInfo);
                }
            } else {
                blocksInfo.add(new DigTaskBlockInfo(pos));
            }
        });
        return blocksInfo;
    }

    private Iterable<BlockPos> getBlocksForPart(Pair<BlockPos, BlockPos> part) {
        if(selectionType == SelectionType.LADDER) {
            return PathUtils.getLadderSelection(this.startingBlock, part.getFirst(), part.getSecond(), Direction.Axis.X);
        } else if(selectionType == SelectionType.LADDER_Z_DIRECTION) {
            return PathUtils.getLadderSelection(this.startingBlock, part.getFirst(), part.getSecond(), Direction.Axis.Z);
        } else {
            return PathUtils.fromStartToEnd(part.getFirst(), part.getSecond(), selectionType);
        }
    }

}
