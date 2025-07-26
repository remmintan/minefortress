package net.remmintan.gobi.helpers

import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import net.remmintan.mods.minefortress.core.interfaces.resources.server.IServerResourceHelper
import net.remmintan.mods.minefortress.core.utils.isSurvivalFortress
import java.util.*


class TreeFinder(private val world: World, alreadySelectedLogs: Set<BlockPos> = emptySet()) {

    private val globalProcessedLogs = mutableSetOf<BlockPos>()

    init {
        globalProcessedLogs.addAll(alreadySelectedLogs)
    }

    fun findTree(pos: BlockPos): TreeData? {
        val logs = findConnectedLogsBFS(pos)
        if (logs.isEmpty()) return null
        val leaves = findAssociatedLeavesBFS(logs)
        if (leaves.isEmpty()) return null
        val root = findRootOfTree(logs)
        return TreeData(logs, leaves, root)
    }

    fun visited(pos: BlockPos) = globalProcessedLogs.contains(pos)

    private fun findConnectedLogsBFS(
        startPos: BlockPos
    ): Set<BlockPos> {
        val startState = world.getBlockState(startPos)
        val startBlock = startState.block
        if (!isTreeLog(startBlock)) return emptySet()

        val connectedLogs = mutableSetOf<BlockPos>()
        val queue: Queue<BlockPos> = LinkedList()

        // Check if already globally processed *before* starting BFS
        if (globalProcessedLogs.contains(startPos)) return emptySet()

        queue.offer(startPos)
        globalProcessedLogs.add(startPos) // Mark globally

        while (queue.isNotEmpty()) {
            val currentPos = queue.poll()
            connectedLogs.add(currentPos)

            for (neighborPos in iterateAround(currentPos)) {
                if (!globalProcessedLogs.contains(neighborPos)) {
                    val neighborState = world.getBlockState(neighborPos)
                    if (isTreeLog(neighborState.block) && areEqualLogTypes(startBlock, neighborState.block)) {
                        globalProcessedLogs.add(neighborPos) // Mark globally
                        queue.offer(neighborPos)
                    }
                }
            }
        }
        return connectedLogs
    }

    private fun findAssociatedLeavesBFS(
        treeLogBlocks: Set<BlockPos> // Logs identified in Phase 1
    ): Set<BlockPos> {
        if (treeLogBlocks.isEmpty()) return emptySet()

        // This set serves as BOTH the result set AND the visited set for this leaf BFS.
        val associatedLeaves = mutableSetOf<BlockPos>()
        val queue: Queue<Pair<BlockPos, Int>> = LinkedList() // Pair: (Position, DistanceFromLog)


        for (neighborPos in iterateAroundEach(treeLogBlocks)) {

            // Check if already added to results/visited set
            if (!associatedLeaves.contains(neighborPos)) {
                val neighborState = world.getBlockState(neighborPos)
                if (isTreeLeaf(neighborState.block)) {
                    // Add to results/visited set AND queue
                    associatedLeaves.add(neighborPos)
                    queue.offer(neighborPos to 1) // Initial distance is 1
                }
            }
        }

        // 2. BFS for leaves
        while (queue.isNotEmpty()) {
            val (currentLeafPos, currentDistance) = queue.poll()
            if (currentDistance >= 6) continue

            for (leafNeighborPos in iterateAround(currentLeafPos)) {
                if (!associatedLeaves.contains(leafNeighborPos)) {
                    val leafNeighborState = world.getBlockState(leafNeighborPos)
                    if (isTreeLeaf(leafNeighborState.block)) {
                        val distanceToThisLeaf = currentDistance + 1

                        if (hasForeignLogNearby(leafNeighborPos, distanceToThisLeaf - 1, treeLogBlocks)) continue
                        associatedLeaves.add(leafNeighborPos)
                        queue.offer(leafNeighborPos to distanceToThisLeaf)
                    }
                }
            }
        }

        return associatedLeaves
    }

    private fun hasForeignLogNearby(
        centerPos: BlockPos,
        radius: Int,
        originalTreeLogs: Set<BlockPos>
    ): Boolean {
        for (nearbyPos in iterateAround(centerPos, radius)) {
            val nearbyState = world.getBlockState(nearbyPos)
            if (isTreeLog(nearbyState.block) && !originalTreeLogs.contains(nearbyPos)) {
                return true
            }
        }
        return false
    }

    private fun findRootOfTree(treeLogBlocks: Set<BlockPos>): BlockPos {
        return treeLogBlocks.minOf { it.y }.let { minY -> treeLogBlocks.first { it.y == minY } }
    }

    private fun isTreeLog(block: Block) = block.defaultState.isIn(BlockTags.LOGS)

    private fun isTreeLeaf(block: Block) =
        block.defaultState.isIn(BlockTags.LEAVES) || block.defaultState.isIn(BlockTags.BEEHIVES)

    private fun areEqualLogTypes(one: Block, two: Block) = one.defaultState.isOf(two)

    private fun iterateAroundEach(blocksPos: Set<BlockPos>) = sequence {
        for (blockPos in blocksPos) {
            yieldAll(iterateAround(blockPos))
        }
    }

    private fun iterateAround(blockPos: BlockPos, radius: Int = 1) = sequence<BlockPos> {
        if (radius < 1) return@sequence
        val pos = blockPos.toImmutable()
        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                for (dz in -radius..radius) {
                    if (dx == 0 && dy == 0 && dz == 0) continue
                    val nearbyPos = pos.add(dx, dy, dz)
                    if (!world.isInBuildLimit(nearbyPos)) continue
                    yield(nearbyPos)
                }
            }
        }
    }
}

class TreeRemover(
    private val world: ServerWorld,
    private val resourceHelper: IServerResourceHelper,
    private val entity: LivingEntity? = null
) {

    fun removeTheTree(tree: TreeData) {
        if (world.getBlockState(tree.treeRootBlock).isIn(BlockTags.LOGS)) {
            removeBlockAddDropToTheResources(tree.treeRootBlock)
        }
        tree.treeLogBlocks
            .filter { it != tree.treeRootBlock }
            .forEach { removeBlockAddDropToTheResources(it) }
        tree.treeLeavesBlocks
            .forEach { removeBlockAddDropToTheResources(it) }
    }

    private fun removeBlockAddDropToTheResources(blockPos: BlockPos) {
        addDropToTheResourceManager(blockPos)
        world.setBlockState(blockPos, Blocks.AIR.defaultState, 3)
        world.emitGameEvent(entity, GameEvent.BLOCK_DESTROY, blockPos)
    }

    private fun addDropToTheResourceManager(pos: BlockPos) {
        if (world.server.isSurvivalFortress()) {
            val blockState = world.getBlockState(pos)
            val blockEntity = world.getBlockEntity(pos)
            // FIXME: consider the tool and the entity
            val drop = Block.getDroppedStacks(blockState, world, pos, blockEntity)
            resourceHelper.putItemsToSuitableContainer(drop)
        }
    }
}

