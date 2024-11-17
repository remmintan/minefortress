package org.minefortress.registries.events

import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.util.ActionResult
import net.remmintan.mods.minefortress.blocks.FortressBlocks
import net.remmintan.mods.minefortress.blueprints.getPersonalBlueprintCell
import net.remmintan.mods.minefortress.blueprints.isBlueprintWorld

fun registerPlayerBlockEvents() {
    UseBlockCallback.EVENT.register { player, world, hand, hitResult ->

        val cell = player.getPersonalBlueprintCell()

        return@register if (!world.isBlueprintWorld() || world.getBlockState(hitResult.blockPos)
                .isOf(FortressBlocks.FORTRESS_BUILDING)
        )
            ActionResult.PASS
        else if (!cell.contains(hitResult.blockPos))
            ActionResult.FAIL
        else ActionResult.PASS
    }

    AttackBlockCallback.EVENT.register { player, world, hand, blockPos, direction ->
        val cell = player.getPersonalBlueprintCell()

        return@register if (!world.isBlueprintWorld())
            ActionResult.PASS
        else if (!cell.contains(blockPos))
            ActionResult.FAIL
        else ActionResult.PASS
    }
}