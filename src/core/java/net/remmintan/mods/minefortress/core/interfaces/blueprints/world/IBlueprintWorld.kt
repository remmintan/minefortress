package net.remmintan.mods.minefortress.core.interfaces.blueprints.world

import net.minecraft.block.BlockState
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup

interface IBlueprintWorld {

    fun prepareBlueprint(blueprintId: String?, blueprintName: String?, group: BlueprintGroup?)
    fun clearBlueprint(player: ServerPlayerEntity?)
    fun putBlueprintInAWorld(
        blueprintData: Map<BlockPos, BlockState>,
        player: ServerPlayerEntity?,
        blueprintSize: Vec3i,
        floorLevel: Int
    )

    val world: ServerWorld

}