package org.minefortress.registries.events

import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockBox
import net.remmintan.mods.minefortress.blocks.FortressBlocks
import net.remmintan.mods.minefortress.blueprints.BLUEPRINT_END
import net.remmintan.mods.minefortress.blueprints.BLUEPRINT_START
import net.remmintan.mods.minefortress.blueprints.isBlueprintWorld

fun registerPlayerBlockEvents() {
    UseBlockCallback.EVENT.register { player, world, hand, hitResult ->

        return@register if (!world.isBlueprintWorld() || world.getBlockState(hitResult.blockPos)
                .isOf(FortressBlocks.FORTRESS_BUILDING)
        )
            ActionResult.PASS
        else if (!BlockBox.create(BLUEPRINT_START, BLUEPRINT_END).contains(hitResult.blockPos))
            ActionResult.FAIL
        else ActionResult.PASS
    }

    AttackBlockCallback.EVENT.register { player, world, hand, blockPos, direction ->
        return@register if (!world.isBlueprintWorld())
            ActionResult.PASS
        else if (!BlockBox.create(BLUEPRINT_START, BLUEPRINT_END).contains(blockPos))
            ActionResult.FAIL
        else ActionResult.PASS
    }
}