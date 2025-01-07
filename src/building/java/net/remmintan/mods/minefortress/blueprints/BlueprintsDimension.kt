package net.remmintan.mods.minefortress.blueprints

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import net.remmintan.mods.minefortress.blocks.FortressBlocks
import net.remmintan.mods.minefortress.blocks.FortressBuildingConfigurationBlockEntity
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.BLUEPRINT_DIMENSION_KEY
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val BORDER_STATE = Blocks.RED_WOOL.defaultState

private const val GRID_SIZE = 100000
private const val GRID_SIDE_SIZE = 18
private const val BLUEPRINT_CELL_SIZE = GRID_SIDE_SIZE - 2
const val DEFAULT_FLOOR_LEVEL = 16

data class PersonalizedBlueprintCell(
    val start: BlockPos,
    val end: BlockPos,
    val configBlock: BlockPos,
) {
    private val allowedBox: BlockBox = BlockBox.create(start, end)
    fun contains(pos: BlockPos): Boolean = allowedBox.contains(pos)
}

private val cellsCache = ConcurrentHashMap<UUID, PersonalizedBlueprintCell>()

fun PlayerEntity.getPersonalBlueprintCell(): PersonalizedBlueprintCell {
    return cellsCache.computeIfAbsent(uuid) {
        val gridX = Math.floorMod(it.mostSignificantBits, GRID_SIZE)
        val gridZ = Math.floorMod(it.leastSignificantBits, GRID_SIZE)

        val startX = gridX * GRID_SIDE_SIZE + 1
        val startZ = gridZ * GRID_SIDE_SIZE + 1
        val startBlock = BlockPos(startX, 1, startZ)

        val endX = startX + GRID_SIDE_SIZE - 2
        val endZ = startZ + GRID_SIDE_SIZE - 2
        val endBlock = BlockPos(endX, 29, endZ)

        val configBlock = BlockPos(startX - 1, 16, startZ - 1)

        return@computeIfAbsent PersonalizedBlueprintCell(
            startBlock.toImmutable(),
            endBlock.toImmutable(),
            configBlock.toImmutable()
        )
    }
}

fun World.isBlueprintWorld(): Boolean = this.registryKey == BLUEPRINT_DIMENSION_KEY

fun MinecraftServer.getBlueprintWorld(): ServerWorld =
    this.getWorld(BLUEPRINT_DIMENSION_KEY) ?: error("Blueprint world not found")

fun ServerWorld.setBlueprintMetadata(
    blueprintId: String?,
    blueprintName: String?,
    group: BlueprintGroup?,
    player: ServerPlayerEntity?,
    capacity: Int,
    profession: ProfessionType
) {
    player?.getPersonalBlueprintCell()?.let {
        val metadataPos = it.configBlock
        this.setBlockState(metadataPos, FortressBlocks.FORTRESS_BUILDING_CONFIGURATION.defaultState)
        (this.getBlockEntity(metadataPos) as FortressBuildingConfigurationBlockEntity).apply {
            this.blueprintId = blueprintId
            this.blueprintName = blueprintName
            this.blueprintGroup = group ?: BlueprintGroup.LIVING_HOUSES
            this.capacity = capacity
            this.profession = profession
        }
    } ?: error("Player not found")
}

data class BlueprintWorldMetadata(
    val id: String?,
    val name: String?,
    val group: BlueprintGroup,
    val capacity: Int,
    val profession: ProfessionType
)

fun ServerWorld.getBlueprintMetadata(player: ServerPlayerEntity?): BlueprintWorldMetadata {
    this.isBlueprintWorld() || error("Not a blueprint world")

    player ?: error("Player not found")

    return player.getPersonalBlueprintCell()
        .configBlock
        .let { this.getBlockEntity(it) as FortressBuildingConfigurationBlockEntity }
        .run {
            BlueprintWorldMetadata(
                blueprintId,
                blueprintName,
                blueprintGroup,
                capacity,
                profession
            )
        }

}

fun ServerWorld.clearBlueprint(player: ServerPlayerEntity?) {
    putBlueprintInAWorld(HashMap(), player, Vec3i(1, 1, 1), 0)
}

fun World.getBlueprintMinY(player: ServerPlayerEntity?): Int {
    this.isBlueprintWorld() || error("Not a blueprint world")

    player ?: error("Player not found")

    return player.getPersonalBlueprintCell().let {
        BlockPos
            .iterate(it.start, it.end)
            .map { it.toImmutable() }
            .filter {
                it.y < 16 && !this.getBlockState(it).isIn(BlockTags.DIRT) ||
                        it.y >= 16 && !this.getBlockState(it).isOf(Blocks.AIR)
            }
            .minOf { it.y }
    }
}

fun ServerWorld.putBlueprintInAWorld(
    blueprintData: Map<BlockPos, BlockState>,
    player: ServerPlayerEntity?,
    blueprintSize: Vec3i,
    floorLevel: Int
) {
    player ?: error("Player not found")

    val xOffset = (BLUEPRINT_CELL_SIZE - blueprintSize.x) / 2
    val zOffset = (BLUEPRINT_CELL_SIZE - blueprintSize.z) / 2

    val cell = player.getPersonalBlueprintCell()

    val start = cell.start.subtract(Vec3i(1, 1, 1))
    val end = cell.end.add(Vec3i(1, 1, 1))


    BlockPos
        .iterate(start, end)
        .forEach { pos: BlockPos ->
            val blockState: BlockState?
            val offsetPos = pos
                .down(DEFAULT_FLOOR_LEVEL - floorLevel)
                .add(-xOffset - cell.start.x, 0, -zOffset - cell.start.z)

            if (this.getBlockState(pos).block == FortressBlocks.FORTRESS_BUILDING_CONFIGURATION)
                return@forEach

            blockState = if (pos.y == DEFAULT_FLOOR_LEVEL - 1 &&
                (pos.x == start.x || pos.x == end.x || pos.z == start.z || pos.z == end.z)
            ) {
                BORDER_STATE
            } else if (pos.y >= DEFAULT_FLOOR_LEVEL && pos.x == end.x && pos.z == end.z) {
                BORDER_STATE
            } else if (blueprintData.containsKey(offsetPos)) {
                blueprintData[offsetPos]
            } else if (pos.y >= DEFAULT_FLOOR_LEVEL) {
                Blocks.AIR.defaultState
            } else if (pos.y == 0) {
                Blocks.BEDROCK.defaultState
            } else if (pos.y > 0 && pos.y < DEFAULT_FLOOR_LEVEL - 2) {
                Blocks.DIRT.defaultState
            } else {
                Blocks.GRASS_BLOCK.defaultState
            }

            this.setBlockState(pos, blockState)
            this.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos)
        }
}
