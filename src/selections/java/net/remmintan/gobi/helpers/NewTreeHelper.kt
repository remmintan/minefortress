package net.remmintan.gobi.helpers

import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn
import net.remmintan.mods.minefortress.core.utils.ServerModUtils
import java.util.*

fun findTree(pos: BlockPos, world: World, globalProcessedLogs: MutableSet<BlockPos>): TreeData? {
    val logs = findConnectedLogsBFS(pos, world, globalProcessedLogs)
    if (logs.isEmpty()) return null
    val candidateLeaves = findAssociatedLeavesBFS(world, logs)
    val searchRadius = 2
    val filteredLeaves = candidateLeaves.filterNot { hasForeignLogNearby(world, it, searchRadius, logs) }.toSet()
    val root = findRootOfTree(logs)
    return TreeData(logs, filteredLeaves, root)
}

private fun findConnectedLogsBFS(
    startPos: BlockPos,
    world: World,
    globalProcessedLogs: MutableSet<BlockPos> // Mutated directly
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

        for (dx in -1..1) {
            for (dy in -1..1) {
                for (dz in -1..1) {
                    if (dx == 0 && dy == 0 && dz == 0) continue
                    val neighborPos = currentPos.add(dx, dy, dz)

                    if (!world.isInBuildLimit(neighborPos)) continue

                    if (!globalProcessedLogs.contains(neighborPos)) {
                        val neighborState = world.getBlockState(neighborPos)
                        if (isTreeLog(neighborState.block) && areEqualLogTypes(startBlock, neighborState.block)) {
                            globalProcessedLogs.add(neighborPos) // Mark globally
                            queue.offer(neighborPos)
                        }
                    }
                }
            }
        }
    }
    return connectedLogs
}

private fun findAssociatedLeavesBFS(
    world: World,
    treeLogBlocks: Set<BlockPos> // Logs identified in Phase 1
): Set<BlockPos> {
    if (treeLogBlocks.isEmpty()) return emptySet()

    // This set serves as BOTH the result set AND the visited set for this leaf BFS.
    val associatedLeaves = mutableSetOf<BlockPos>()
    val queue: Queue<Pair<BlockPos, Int>> = LinkedList() // Pair: (Position, DistanceFromLog)


    // 1. Seed the queue with leaves directly adjacent (including diagonals) to the tree's logs
    for (logPos in treeLogBlocks) {
        for (dx in -1..1) {
            for (dy in -1..1) {
                for (dz in -1..1) {
                    if (dx == 0 && dy == 0 && dz == 0) continue

                    val neighborPos = logPos.add(dx, dy, dz)

                    if (!world.isInBuildLimit(neighborPos)) continue

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
            }
        }
    }

    // 2. BFS for leaves
    while (queue.isNotEmpty()) {
        val (currentLeafPos, currentDistance) = queue.poll()
        // currentLeafPos is already in associatedLeaves

        // Stop searching if too far from a log
        if (currentDistance >= 6) continue

        // 3. Explore neighbors (including diagonals)
        for (dx in -1..1) {
            for (dy in -1..1) {
                for (dz in -1..1) {
                    if (dx == 0 && dy == 0 && dz == 0) continue

                    val leafNeighborPos = currentLeafPos.add(dx, dy, dz)

                    if (!world.isInBuildLimit(leafNeighborPos)) continue

                    // Check if neighbor should be added (not already in results/visited)
                    if (!associatedLeaves.contains(leafNeighborPos)) {
                        val leafNeighborState = world.getBlockState(leafNeighborPos)

                        // Crucial Check: Stop if we hit a log NOT part of our tree
                        if (isTreeLog(leafNeighborState.block) && !treeLogBlocks.contains(leafNeighborPos)) {
                            continue // Don't explore further down this path
                        }

                        // If it's a leaf, add it and continue the search
                        if (isTreeLeaf(leafNeighborState.block)) {
                            // Add to results/visited set AND queue
                            associatedLeaves.add(leafNeighborPos)
                            queue.offer(leafNeighborPos to currentDistance + 1)
                        }
                    }
                }
            }
        }
    }

    return associatedLeaves
}

private fun findRootOfTree(treeLogBlocks: Set<BlockPos>): BlockPos {
    return treeLogBlocks.minOf { it.y }.let { minY -> treeLogBlocks.first { it.y == minY } }
}

fun isTreeLog(block: Block): Boolean {
    return block.defaultState.isIn(BlockTags.LOGS)
}

private fun hasForeignLogNearby(
    world: World,
    centerPos: BlockPos,
    radius: Int,
    originalTreeLogs: Set<BlockPos>
): Boolean {
    for (dx in -radius..radius) {
        for (dy in -radius..radius) {
            for (dz in -radius..radius) {
                if (dx == 0 && dy == 0 && dz == 0) continue // Skip self

                val nearbyPos = centerPos.add(dx, dy, dz)
                if (!world.isInBuildLimit(nearbyPos)) continue

                val nearbyState = world.getBlockState(nearbyPos)
                if (isTreeLog(nearbyState.block)) {
                    // Found a log nearby. Is it part of the original tree?
                    if (!originalTreeLogs.contains(nearbyPos)) {
                        // It's a foreign log!
                        return true
                    }
                }
            }
        }
    }
    // No foreign logs found within the radius
    return false
}

private fun isTreeLeaf(block: Block): Boolean {
    return block.defaultState.isIn(BlockTags.LEAVES)
}

private fun areEqualLogTypes(one: Block, two: Block): Boolean {
    return one.defaultState.isOf(two)
}

fun removeTheTree(pawn: IWorkerPawn, tree: TreeData, world: ServerWorld) {
    tree.treeLogBlocks.filter { it != tree.treeRootBlock }.forEach { removeBlockAddDropToTheResources(pawn, world, it) }
    tree.treeLeavesBlocks.forEach { removeBlockAddDropToTheResources(pawn, world, it) }
}

private fun removeBlockAddDropToTheResources(pawn: IWorkerPawn, world: ServerWorld, blockPos: BlockPos) {
    ServerModUtils.addDropToTheResourceManager(world, blockPos, pawn)
    world.setBlockState(blockPos, Blocks.AIR.defaultState, 3)
    world.emitGameEvent(pawn as Entity, GameEvent.BLOCK_DESTROY, blockPos)
}