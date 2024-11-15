package net.remmintan.mods.minefortress.blueprints

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import net.remmintan.mods.minefortress.blocks.BuildingBlockEntity
import net.remmintan.mods.minefortress.blocks.FortressBlocks
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.BLUEPRINT_DIMENSION_KEY

val BLUEPRINT_START = BlockPos(0, 1, 0)
val BLUEPRINT_END = BlockPos(15, 32, 15)

private val BORDER_STATE = Blocks.RED_WOOL.defaultState

private val METADATA_POS = BlockPos(-1, 16, -1)

fun World.isBlueprintWorld(): Boolean = this.registryKey == BLUEPRINT_DIMENSION_KEY

fun MinecraftServer.getBlueprintWorld(): ServerWorld =
    this.getWorld(BLUEPRINT_DIMENSION_KEY) ?: error("Blueprint world not found")

fun ServerWorld.setBlueprintMetadata(blueprintId: String?, blueprintName: String?, group: BlueprintGroup?) {
    this.setBlockState(METADATA_POS, FortressBlocks.FORTRESS_BUILDING.defaultState)
    (this.getBlockEntity(METADATA_POS) as BuildingBlockEntity).apply {
        this.blueprintId = blueprintId
        this.blueprintName = blueprintName
        this.blueprintGroup = group ?: BlueprintGroup.LIVING_HOUSES
    }
}

data class BlueprintWorldMetadata(
    val id: String?,
    val name: String?,
    val group: BlueprintGroup,
    val capacity: Int,
    val profession: ProfessionType
)

fun ServerWorld.getBlueprintMetadata(): BlueprintWorldMetadata {
    this.isBlueprintWorld() || error("Not a blueprint world")

    val blockEntity = this.getBlockEntity(METADATA_POS) as BuildingBlockEntity
    return BlueprintWorldMetadata(
        blockEntity.blueprintId,
        blockEntity.blueprintName,
        blockEntity.blueprintGroup,
        blockEntity.capacity,
        blockEntity.profession
    )
}

fun ServerWorld.clearBlueprint(player: ServerPlayerEntity?) {
    putBlueprintInAWorld(HashMap(), player, Vec3i(1, 1, 1), 0)
}

fun ServerWorld.putBlueprintInAWorld(
    blueprintData: Map<BlockPos, BlockState>,
    player: ServerPlayerEntity?,
    blueprintSize: Vec3i,
    floorLevel: Int
) {
    val xOffset = (16 - blueprintSize.x) / 2
    val zOffset = (16 - blueprintSize.z) / 2

    val defaultFloorLevel = 16
    BlockPos
        .iterate(BlockPos(-32, 0, -32), BlockPos(32, 32, 32))
        .forEach { pos: BlockPos ->
            val blockState: BlockState?
            val offsetPos = pos
                .down(defaultFloorLevel - floorLevel)
                .add(-xOffset, 0, -zOffset)

            if (this.getBlockState(pos).block == FortressBlocks.FORTRESS_BUILDING)
                return@forEach

            blockState = if (blueprintData.containsKey(offsetPos)) {
                blueprintData[offsetPos]
            } else if (pos.y >= defaultFloorLevel) {
                Blocks.AIR.defaultState
            } else if (pos.y == 0) {
                Blocks.BEDROCK.defaultState
            } else if (pos.y > 0 && pos.y < defaultFloorLevel - 2) {
                Blocks.DIRT.defaultState
            } else {
                Blocks.GRASS_BLOCK.defaultState
            }

            this.setBlockState(pos, blockState)
            this.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos)
        }

    BlockPos.iterate(BlockPos(-1, 15, -1), BlockPos(16, 15, 16)).forEach { pos: BlockPos ->
        if (pos.z == -1 || pos.z == 16 || pos.x == -1 || pos.x == 16) {
            this.setBlockState(pos, BORDER_STATE)
            this.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos)
        }
    }

    BlockPos.iterate(BlockPos(16, 15, 16), BlockPos(16, 31, 16)).forEach { pos: BlockPos? ->
        this.setBlockState(pos, BORDER_STATE)
        this.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos)
    }
}
