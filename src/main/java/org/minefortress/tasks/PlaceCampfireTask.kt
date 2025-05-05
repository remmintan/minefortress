package org.minefortress.tasks

import net.minecraft.block.BlockState
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.event.GameEvent
import net.remmintan.gobi.helpers.TreeFinder
import net.remmintan.gobi.helpers.TreeRemover
import net.remmintan.mods.minefortress.blocks.FortressBlocks
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager
import net.remmintan.mods.minefortress.core.interfaces.tasks.IPlaceCampfireTask
import net.remmintan.mods.minefortress.core.utils.getFortressManager
import net.remmintan.mods.minefortress.core.utils.getManagersProvider
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper
import net.remmintan.mods.minefortress.networking.s2c.S2CStartFortressConfiguration

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

        val stableFortressCenter = fortressPos ?: error("Fortress pos is null")
        world.server.getManagersProvider(stableFortressCenter)?.let {
            it.buildingsManager?.addBuilding(
                metadata,
                start,
                end,
                blockData
            )
            it.professionsManager?.sendProfessions(player)
            removeAllTreesInTheRadius(world, stableFortressCenter, it.resourceManager)
        }

        val fortressManager = world.server.getFortressManager(stableFortressCenter)
        fortressManager?.spawnInitialPawns()
        fortressManager?.let {
            val packet = S2CStartFortressConfiguration()
            FortressServerNetworkHelper.send(player, S2CStartFortressConfiguration.CHANNEL, packet)
        }

        return stableFortressCenter
    }

    private fun removeAllTreesInTheRadius(
        world: ServerWorld,
        center: BlockPos,
        resourceManager: IServerResourceManager
    ) {
        val c = center.toImmutable()
        val tf = TreeFinder(world)
        val tr = TreeRemover(world, resourceManager)
        val r = 20
        BlockPos.iterate(c.add(-r, -r, -r), c.add(r, r, r))
            .asSequence()
            .map { it.toImmutable() }
            .filter { pos -> center.getSquaredDistance(pos) <= r * r }
            .filter { world.isInBuildLimit(it) }
            .map { tf.findTree(it) }
            .filterNotNull()
            .forEach { tr.removeTheTree(it) }
    }

}