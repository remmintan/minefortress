package org.minefortress.tasks

import com.mojang.datafixers.util.Pair
import net.minecraft.item.Item
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.remmintan.mods.minefortress.core.TaskType
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn
import net.remmintan.mods.minefortress.core.interfaces.selections.ServerSelectionType
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart
import net.remmintan.mods.minefortress.core.utils.PathUtils
import org.minefortress.entity.Colonist
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo
import org.minefortress.tasks.block.info.DigTaskBlockInfo
import org.minefortress.tasks.block.info.ItemTaskBlockInfo
import org.minefortress.utils.BlockInfoUtils

class SimpleSelectionTask(
    taskType: TaskType,
    startingBlock: BlockPos,
    endingBlock: BlockPos,
    hitResult: HitResult,
    private val selectionType: ServerSelectionType,
    positions: List<BlockPos>,
    placingItem: Item?
) :
    AbstractTask(taskType, startingBlock, endingBlock) {
    private var hitResult: HitResult? = null
    private var horizontalDirection: Direction? = null

    val placingItem: Item?
    override val positions: List<BlockPos>

    init {
        val shouldSwapStartAndEnd = isShouldSwapEnds(taskType, startingBlock, endingBlock, selectionType)
        if (shouldSwapStartAndEnd) {
            this.startingBlock = endingBlock
            this.endingBlock = startingBlock
        } else {
            this.startingBlock = startingBlock
            this.endingBlock = endingBlock
        }

        if (selectionType == ServerSelectionType.LADDER && hitResult is BlockHitResult) {
            this.horizontalDirection = if (startingBlock.x > endingBlock.x) Direction.WEST else Direction.EAST
            this.hitResult =
                BlockHitResult(hitResult.getPos(), Direction.UP, hitResult.blockPos, hitResult.isInsideBlock)
        } else if (selectionType == ServerSelectionType.LADDER_Z_DIRECTION && hitResult is BlockHitResult) {
            this.horizontalDirection = if (startingBlock.z > endingBlock.z) Direction.NORTH else Direction.SOUTH
            this.hitResult =
                BlockHitResult(hitResult.getPos(), Direction.UP, hitResult.blockPos, hitResult.isInsideBlock)
        } else {
            this.hitResult = hitResult
            this.horizontalDirection = null
        }

        this.positions = positions
        this.placingItem = placingItem
    }

    private fun isShouldSwapEnds(
        taskType: TaskType,
        startingBlock: BlockPos,
        endingBlock: BlockPos,
        selectionType: ServerSelectionType
    ): Boolean {
        val removeFromBottomToTop = taskType == TaskType.REMOVE && endingBlock.y > startingBlock.y
        val buildFromTopToBottom = taskType == TaskType.BUILD && endingBlock.y < startingBlock.y
        val isLadder =
            selectionType == ServerSelectionType.LADDER || selectionType == ServerSelectionType.LADDER_Z_DIRECTION
        return !isLadder && (removeFromBottomToTop || buildFromTopToBottom)
    }

    override fun prepareTask() {
        if (selectionType == ServerSelectionType.WALLS_EVERY_SECOND) {
            parts.add(Pair.of(startingBlock, endingBlock))
            super.totalParts = 1
        } else {
            super.prepareTask()
        }
    }

    override fun getNextPart(colonist: IWorkerPawn): ITaskPart {
        val startAndEnd = parts.poll()
        checkNotNull(startAndEnd) { "Null part for task!" }
        val blocks = getPartBlocksInfo(startAndEnd, colonist)
        return TaskPart(startAndEnd, blocks, this)
    }

    private fun getPartBlocksInfo(startAndEnd: Pair<BlockPos, BlockPos>, worker: IWorkerPawn): List<ITaskBlockInfo> {
        val blocksInfo: MutableList<ITaskBlockInfo> = ArrayList()
        getBlocksForPart(startAndEnd).spliterator().forEachRemaining { pos: BlockPos ->
            val immutablePos = pos.toImmutable()
            if (placingItem != null) {
                if (BlockInfoUtils.shouldBePlacedAsItem(placingItem)) {
                    val useOnContext =
                        BlockInfoUtils.getUseOnContext(
                            this.hitResult,
                            placingItem,
                            immutablePos,
                            worker.serverWorld,
                            worker as Colonist
                        )
                    val itemTaskBlockInfo = ItemTaskBlockInfo(placingItem, immutablePos, useOnContext)
                    blocksInfo.add(itemTaskBlockInfo)
                } else {
                    val blockStateForPlacement = BlockInfoUtils.getBlockStateForPlacement(
                        placingItem,
                        hitResult,
                        horizontalDirection,
                        immutablePos,
                        worker as Colonist
                    )
                    val blockStateTaskBlockInfo =
                        BlockStateTaskBlockInfo(placingItem, immutablePos, blockStateForPlacement)
                    blocksInfo.add(blockStateTaskBlockInfo)
                }
            } else {
                blocksInfo.add(DigTaskBlockInfo(immutablePos))
            }
        }
        return blocksInfo
    }

    fun getBlocksForPart(part: Pair<BlockPos, BlockPos>): Iterable<BlockPos> {
        return if (selectionType == ServerSelectionType.LADDER) {
            PathUtils.getLadderSelection(
                this.startingBlock,
                part.first,
                part.second,
                Direction.Axis.X
            )
        } else if (selectionType == ServerSelectionType.LADDER_Z_DIRECTION) {
            PathUtils.getLadderSelection(
                this.startingBlock,
                part.first,
                part.second,
                Direction.Axis.Z
            )
        } else {
            PathUtils.fromStartToEnd(
                part.first,
                part.second,
                selectionType == ServerSelectionType.WALLS_EVERY_SECOND
            )
        }
    }

    override fun toTaskInformationDto(): TaskInformationDto {
        return TaskInformationDto(pos, positions, taskType)
    }
}
