package org.minefortress.blueprints.world

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtList
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.remmintan.mods.minefortress.blueprints.*
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.IBlueprintWorld
import net.remmintan.mods.minefortress.core.interfaces.entities.player.FortressServerPlayerEntity

class BlueprintWorldWrapper(server: MinecraftServer) : IBlueprintWorld {

    override val world: ServerWorld = server.getBlueprintWorld()
    override fun setBlueprintMetadata(blueprintId: String?, blueprintName: String?, group: BlueprintGroup?) {
        world.setBlueprintMetadata(blueprintId, blueprintName, group)
    }

    override fun clearBlueprint(player: ServerPlayerEntity?) {
        world.clearBlueprint(player)
    }

    override fun putBlueprintInAWorld(
        blueprintData: Map<BlockPos, BlockState>,
        player: ServerPlayerEntity?,
        blueprintSize: Vec3i,
        floorLevel: Int
    ) {
        world.putBlueprintInAWorld(blueprintData, player, blueprintSize, floorLevel)
    }

    override fun saveBlueprintFromWorld(server: MinecraftServer, player: ServerPlayerEntity) {
        val metadata = server.getBlueprintWorld().getBlueprintMetadata()

        val blueprintId = metadata.id
        val blueprintName = metadata.name

        val id = blueprintId ?: blueprintName?.replace(Regex("[^a-zA-Z0-9]"), "_")?.lowercase()
        ?: throw IllegalStateException("Both name and id are null")

        val updatedStructureIdentifier = Identifier.of("minefortress", id)
        val structureManager = server.structureTemplateManager
        val structure = structureManager.getTemplateOrBlank(updatedStructureIdentifier)

        val dimensions = BLUEPRINT_START.subtract(BLUEPRINT_END).add(1, 1, 1)

        structure.saveFromWorld(player.world, BLUEPRINT_START, dimensions, true, Blocks.DIRT)
        structure.blockInfoLists.forEach {
            it.all.removeIf {
                it.state.isIn(BlockTags.DIRT) || it.state.isOf(Blocks.AIR)
            }
        }

        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        var minZ = Int.MAX_VALUE

        var maxX = Int.MIN_VALUE
        var maxY = Int.MIN_VALUE
        var maxZ = Int.MIN_VALUE

        structure.blockInfoLists.forEach {
            it.all.forEach {
                val pos = it.pos

                minX = minOf(minX, pos.x)
                minY = minOf(minY, pos.y)
                minZ = minOf(minZ, pos.z)

                maxX = maxOf(maxX, pos.x)
                maxY = maxOf(maxY, pos.y)
                maxZ = maxOf(maxZ, pos.z)
            }
        }


        val updatedStructure = NbtCompound()
        structure.writeNbt(updatedStructure)
        val size = NbtList()
        size.add(NbtInt.of(maxX - minX + 1))
        size.add(NbtInt.of(maxY - minY + 1))
        size.add(NbtInt.of(maxZ - minZ + 1))
        updatedStructure.put("size", size)
        if (player is FortressServerPlayerEntity) {
            player._ServerBlueprintManager.update(
                id,
                blueprintName,
                metadata.group,
                updatedStructure,
                16 - minY
            )
        }
    }

}

