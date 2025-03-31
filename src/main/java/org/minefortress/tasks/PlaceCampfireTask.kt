package org.minefortress.tasks

import net.minecraft.block.BlockState
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.event.GameEvent
import net.remmintan.mods.minefortress.blocks.FortressBlocks
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata
import net.remmintan.mods.minefortress.core.interfaces.tasks.IPlaceCampfireTask
import net.remmintan.mods.minefortress.core.utils.getFortressManager
import net.remmintan.mods.minefortress.core.utils.getManagersProvider

class PlaceCampfireTask(
    private val metadata: BlueprintMetadata,
    private val blockData: Map<BlockPos, BlockState>,
    placePos: BlockPos
) : IPlaceCampfireTask {
    override val start: BlockPos
    override val end: BlockPos
    private val blocks = HashMap<BlockPos, BlockState>()


    init {
        blockData.forEach { (pos, blockState) ->
            blocks[placePos.add(pos)] = blockState
        }

        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        var minZ = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var maxY = Int.MIN_VALUE
        var maxZ = Int.MIN_VALUE

        for (blockPos in blocks.keys) {
            if (blockPos.x < minX) minX = blockPos.x
            if (blockPos.y < minY) minY = blockPos.y
            if (blockPos.z < minZ) minZ = blockPos.z
            if (blockPos.x > maxX) maxX = blockPos.x
            if (blockPos.y > maxY) maxY = blockPos.y
            if (blockPos.z > maxZ) maxZ = blockPos.z
        }

        start = BlockPos(minX, minY, minZ)
        end = BlockPos(maxX, maxY, maxZ)
    }

    override fun execute(
        world: ServerWorld,
        player: ServerPlayerEntity,
    ): BlockPos {

        var fortressPos: BlockPos? = null

        BlockPos.iterate(start, end).forEach { pos ->
            val blockState = blocks[pos] ?: return@forEach
            world.setBlockState(pos, blockState, 3)
            world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos)
            world.markDirty(pos)

            if (blockState.isOf(FortressBlocks.FORTRESS_CAMPFIRE)) {
                fortressPos = pos.toImmutable()
            }
        }


        fortressPos?.let { world.server.getManagersProvider(it) }
            ?.buildingsManager
            ?.addBuilding(
                metadata,
                start,
                end,
                blockData
            )

        fortressPos?.let { world.server.getFortressManager(it) }?.spawnInitialPawns()

        return fortressPos ?: error("Fortress pos is null")
    }

}