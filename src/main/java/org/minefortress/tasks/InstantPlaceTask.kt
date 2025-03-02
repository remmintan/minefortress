package org.minefortress.tasks

import net.minecraft.block.BlockState
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.event.GameEvent
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata
import net.remmintan.mods.minefortress.core.interfaces.buildings.IServerBuildingsManager
import net.remmintan.mods.minefortress.core.interfaces.entities.player.IFortressPlayerEntity
import net.remmintan.mods.minefortress.core.interfaces.tasks.IInstantTask

class InstantPlaceTask(
    private val metadata: BlueprintMetadata,
    private val blockData: Map<BlockPos, BlockState>,
    placePos: BlockPos
) : IInstantTask {
    override val start: BlockPos
    override val end: BlockPos
    private val blocks = HashMap<BlockPos, BlockState>()

    private val listeners = ArrayList<() -> Unit>()

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
        getBuildingsManager: () -> IServerBuildingsManager
    ) {
        BlockPos.iterate(start, end).forEach { pos ->
            val blockState = blocks[pos] ?: return@forEach
            world.setBlockState(pos, blockState, 3)
            world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos)
            world.markDirty(pos)
        }

        getBuildingsManager().addBuilding(
            (player as IFortressPlayerEntity).get_FortressPos().orElseThrow(),
            metadata,
            start,
            end,
            blockData
        )
        listeners.forEach { it() }

    }

    override fun addFinishListener(listener: () -> Unit) {
        this.listeners.add(listener)
    }
}