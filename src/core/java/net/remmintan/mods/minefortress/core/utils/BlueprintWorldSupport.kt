package net.remmintan.mods.minefortress.core.utils

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.entities.player.FortressServerPlayerEntity
import net.remmintan.mods.minefortress.core.interfaces.server.IBlueprintEditingWorld
import kotlin.math.max
import kotlin.math.min


fun saveBlueprintFromWorld(server: MinecraftServer, player: ServerPlayerEntity) {
    val fortressServerWorld = player.world as IBlueprintEditingWorld

    val blueprintId = fortressServerWorld.blueprintId
    val blueprintName = fortressServerWorld.blueprintName

    val idStr = blueprintId ?: blueprintName?.replace(Regex("[^a-zA-Z0-9]"), "_")?.lowercase()
    ?: throw IllegalStateException("Both name and id are null")

    val updatedStructureIdentifier = Identifier.of("minefortress", idStr)
    val structureManager = server.structureTemplateManager
    val structureToUpdate = structureManager.getTemplateOrBlank(updatedStructureIdentifier)
    fortressServerWorld.enableSaveStructureMode()

    val start = BlockPos(0, 1, 0)
    val end = BlockPos(15, 32, 15)
    val allPositions = BlockPos.iterate(start, end)

    var minX = Int.MAX_VALUE
    var minY = Int.MAX_VALUE
    var minZ = Int.MAX_VALUE

    var maxX = Int.MIN_VALUE
    var maxY = Int.MIN_VALUE
    var maxZ = Int.MIN_VALUE

    for (pos in allPositions) {
        val blockState = player.world.getBlockState(pos)
        val y = pos.y
        if (isStateWasChanged(blockState, y)) {
            minX = min(minX.toDouble(), pos.x.toDouble()).toInt()
            minY = min(minY.toDouble(), pos.y.toDouble()).toInt()
            minZ = min(minZ.toDouble(), pos.z.toDouble()).toInt()

            maxX = max(maxX.toDouble(), pos.x.toDouble()).toInt()
            maxY = max(maxY.toDouble(), pos.y.toDouble()).toInt()
            maxZ = max(maxZ.toDouble(), pos.z.toDouble()).toInt()
        }
    }

    val min = BlockPos(minX, minY, minZ)
    val max = BlockPos(maxX, maxY, maxZ)
    val dimensions = max.subtract(min).add(1, 1, 1)

    structureToUpdate.saveFromWorld(player.world, min, dimensions, true, Blocks.VOID_AIR)
    fortressServerWorld.disableSaveStructureMode()

    val newFloorLevel = 16 - min.y

    val updatedStructure = NbtCompound()
    structureToUpdate.writeNbt(updatedStructure)
    if (player is FortressServerPlayerEntity) {
        player._ServerBlueprintManager.update(
            idStr,
            blueprintName,
            fortressServerWorld.blueprintGroup,
            updatedStructure,
            newFloorLevel
        )
    }

}

private fun isStateWasChanged(blockState: BlockState, y: Int): Boolean {
    if (blockState.isOf(Blocks.VOID_AIR)) return false
    if (y > 15) return !blockState.isAir
    if (y == 15) return !blockState.isOf(Blocks.GRASS_BLOCK)
    return !blockState.isOf(Blocks.DIRT)
}

