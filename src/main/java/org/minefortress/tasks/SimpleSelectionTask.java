package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.interfaces.IWorkerPawn;
import org.minefortress.selections.ServerSelectionType;
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
    private final ServerSelectionType selectionType;

    private Item placingItem;

    public SimpleSelectionTask(UUID id, TaskType taskType, BlockPos startingBlock, BlockPos endingBlock, HitResult hitResult, ServerSelectionType selectionType) {
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

        if(selectionType == ServerSelectionType.LADDER && hitResult instanceof BlockHitResult) {
            this.horizontalDirection  = startingBlock.getX() > endingBlock.getX() ? Direction.WEST : Direction.EAST;
            this.hitResult = new BlockHitResult(hitResult.getPos(), Direction.UP, ((BlockHitResult) hitResult).getBlockPos(), ((BlockHitResult) hitResult).isInsideBlock());
        } else if (selectionType == ServerSelectionType.LADDER_Z_DIRECTION && hitResult instanceof BlockHitResult) {
            this.horizontalDirection  = startingBlock.getZ() > endingBlock.getZ() ? Direction.NORTH : Direction.SOUTH;
            this.hitResult = new BlockHitResult(hitResult.getPos(), Direction.UP, ((BlockHitResult) hitResult).getBlockPos(), ((BlockHitResult) hitResult).isInsideBlock());
        } else {
            this.hitResult = hitResult;
            this.horizontalDirection = null;
        }
    }

    private boolean isShouldSwapEnds(TaskType taskType, BlockPos startingBlock, BlockPos endingBlock, ServerSelectionType selectionType) {
        boolean removeFromBottomToTop = taskType == TaskType.REMOVE && endingBlock.getY() > startingBlock.getY();
        boolean buildFromTopToBottom = taskType == TaskType.BUILD && endingBlock.getY() < startingBlock.getY();
        boolean isLadder = selectionType == ServerSelectionType.LADDER || selectionType == ServerSelectionType.LADDER_Z_DIRECTION;
        return !isLadder && (removeFromBottomToTop || buildFromTopToBottom);
    }

    @Override
    public void prepareTask() {
        if(selectionType == ServerSelectionType.WALLS_EVERY_SECOND) {
            parts.add(Pair.of(startingBlock, endingBlock));
            super.totalParts = 1;
        } else {
            super.prepareTask();
        }
    }

    public void setPlacingItem(Item placingItem) {
        this.placingItem = placingItem;
    }

    public Item getPlacingItem() {
        return placingItem;
    }

    @Override
    public TaskPart getNextPart(ServerWorld world, IWorkerPawn colonist) {
        Pair<BlockPos, BlockPos> startAndEnd = parts.poll();
        if(startAndEnd == null) throw new IllegalStateException("Null part for task!");
        final List<TaskBlockInfo> blocks = getPartBlocksInfo(startAndEnd, world, colonist);
        return new TaskPart(startAndEnd, blocks, this);
    }

    private List<TaskBlockInfo> getPartBlocksInfo(Pair<BlockPos, BlockPos> startAndEnd, ServerWorld world, IWorkerPawn colonist) {
        final List<TaskBlockInfo> blocksInfo = new ArrayList<>();
        getBlocksForPart(startAndEnd).spliterator().forEachRemaining(pos -> {
            pos = pos.toImmutable();
            if(placingItem != null) {
                if(BlockInfoUtils.shouldBePlacedAsItem(placingItem)) {
                    final ItemUsageContext useOnContext = BlockInfoUtils.getUseOnContext(this.hitResult, this.placingItem, pos, world, (Colonist) colonist);
                    final ItemTaskBlockInfo itemTaskBlockInfo = new ItemTaskBlockInfo(placingItem, pos, useOnContext);
                    blocksInfo.add(itemTaskBlockInfo);
                } else {
                    final BlockState blockStateForPlacement = BlockInfoUtils.getBlockStateForPlacement(placingItem, hitResult, horizontalDirection, pos, (Colonist) colonist);
                    final BlockStateTaskBlockInfo blockStateTaskBlockInfo = new BlockStateTaskBlockInfo(placingItem, pos, blockStateForPlacement);
                    blocksInfo.add(blockStateTaskBlockInfo);
                }
            } else {
                blocksInfo.add(new DigTaskBlockInfo(pos));
            }
        });
        return blocksInfo;
    }

    public Iterable<BlockPos> getBlocksForPart(Pair<BlockPos, BlockPos> part) {
        if(selectionType == ServerSelectionType.LADDER) {
            return PathUtils.getLadderSelection(this.startingBlock, part.getFirst(), part.getSecond(), Direction.Axis.X);
        } else if(selectionType == ServerSelectionType.LADDER_Z_DIRECTION) {
            return PathUtils.getLadderSelection(this.startingBlock, part.getFirst(), part.getSecond(), Direction.Axis.Z);
        } else {
            return PathUtils.fromStartToEnd(part.getFirst(), part.getSecond(), selectionType);
        }
    }

}
