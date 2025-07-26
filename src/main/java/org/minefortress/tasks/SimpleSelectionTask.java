package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;
import net.remmintan.mods.minefortress.core.interfaces.selections.ServerSelectionType;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart;
import net.remmintan.mods.minefortress.core.utils.PathUtils;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.Colonist;
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo;
import org.minefortress.tasks.block.info.DigTaskBlockInfo;
import org.minefortress.tasks.block.info.ItemTaskBlockInfo;
import org.minefortress.utils.BlockInfoUtils;

import java.util.ArrayList;
import java.util.List;

public class SimpleSelectionTask extends AbstractTask {

    private final HitResult hitResult;
    private final Direction horizontalDirection;
    private final ServerSelectionType selectionType;

    private final Item placingItem;
    private final List<BlockPos> positions;

    public SimpleSelectionTask(TaskType taskType, BlockPos startingBlock, BlockPos endingBlock, HitResult hitResult, ServerSelectionType selectionType, List<BlockPos> positions, Item placingItem) {
        super(taskType, startingBlock, endingBlock);
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

        this.positions = positions;
        this.placingItem = placingItem;
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

    public Item getPlacingItem() {
        return placingItem;
    }

    @Override
    public ITaskPart getNextPart(IWorkerPawn colonist) {
        Pair<BlockPos, BlockPos> startAndEnd = parts.poll();
        if(startAndEnd == null) throw new IllegalStateException("Null part for task!");
        final List<ITaskBlockInfo> blocks = getPartBlocksInfo(startAndEnd, colonist);
        return new TaskPart(startAndEnd, blocks, this);
    }

    private List<ITaskBlockInfo> getPartBlocksInfo(Pair<BlockPos, BlockPos> startAndEnd, IWorkerPawn worker) {
        final List<ITaskBlockInfo> blocksInfo = new ArrayList<>();
        getBlocksForPart(startAndEnd).spliterator().forEachRemaining(pos -> {
            pos = pos.toImmutable();
            if(placingItem != null) {
                if(BlockInfoUtils.shouldBePlacedAsItem(placingItem)) {
                    final ItemUsageContext useOnContext = BlockInfoUtils.getUseOnContext(this.hitResult, this.placingItem, pos, worker.getServerWorld(), (Colonist) worker);
                    final ItemTaskBlockInfo itemTaskBlockInfo = new ItemTaskBlockInfo(placingItem, pos, useOnContext);
                    blocksInfo.add(itemTaskBlockInfo);
                } else {
                    final BlockState blockStateForPlacement = BlockInfoUtils.getBlockStateForPlacement(placingItem, hitResult, horizontalDirection, pos, (Colonist) worker);
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
            return PathUtils.fromStartToEnd(part.getFirst(), part.getSecond(), selectionType==ServerSelectionType.WALLS_EVERY_SECOND);
        }
    }

    @Override
    public @NotNull List<BlockPos> getPositions() {
        return positions;
    }

    @Override
    @NotNull
    public List<TaskInformationDto> toTaskInformationDto() {
        return List.of(new TaskInformationDto(getPos(), getPositions(), taskType));
    }
}
