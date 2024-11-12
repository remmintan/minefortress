package org.minefortress.blueprints.world

import net.minecraft.block.BlockState
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.remmintan.mods.minefortress.blueprints.clearBlueprint
import net.remmintan.mods.minefortress.blueprints.getBlueprintWorld
import net.remmintan.mods.minefortress.blueprints.prepareBlueprint
import net.remmintan.mods.minefortress.blueprints.putBlueprintInAWorld
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.IBlueprintWorld

class BlueprintWorldWrapper(server: MinecraftServer) : IBlueprintWorld {

    override val world: ServerWorld = server.getBlueprintWorld()
    override fun prepareBlueprint(blueprintId: String?, blueprintName: String?, group: BlueprintGroup?) {
        world.prepareBlueprint(blueprintId, blueprintName, group)
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
}