package org.minefortress.registries.events

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents.*
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.remmintan.mods.minefortress.core.isFortressGamemode

object PlayerSleepEvents {

    fun register() {
        ALLOW_BED.register(AllowBed { entity: LivingEntity?, sleepingPos: BlockPos?, state: BlockState?, vanillaResult: Boolean ->
            return@AllowBed if (isFortressGamemode(entity)) {
                ActionResult.SUCCESS
            } else {
                ActionResult.PASS

            }
        })

        MODIFY_SLEEPING_DIRECTION.register(ModifySleepingDirection { entity: LivingEntity, pos: BlockPos?, dir: Direction? ->
            return@ModifySleepingDirection if (isFortressGamemode(entity)) {
                val rotationVector = entity.rotationVector
                Direction.getFacing(rotationVector.x, rotationVector.y, rotationVector.z)
            } else {
                dir
            }
        })

        ALLOW_NEARBY_MONSTERS.register(AllowNearbyMonsters { player: PlayerEntity?, pos: BlockPos?, vanilla: Boolean ->
            return@AllowNearbyMonsters if (isFortressGamemode(player)) {
                ActionResult.SUCCESS
            } else {
                ActionResult.PASS
            }
        })
    }

}