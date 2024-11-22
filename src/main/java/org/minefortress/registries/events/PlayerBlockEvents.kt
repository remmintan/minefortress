package org.minefortress.registries.events

import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.block.BlockState
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.hit.BlockHitResult
import net.remmintan.mods.minefortress.blocks.FortressBlocks
import net.remmintan.mods.minefortress.blueprints.getPersonalBlueprintCell
import net.remmintan.mods.minefortress.blueprints.isBlueprintWorld
import net.remmintan.mods.minefortress.building.BuildingHelper
import net.remmintan.mods.minefortress.core.FortressState
import net.remmintan.mods.minefortress.core.interfaces.buildings.IEssentialBuildingInfo
import net.remmintan.mods.minefortress.core.interfaces.client.IClientFortressManager
import net.remmintan.mods.minefortress.core.utils.CoreModUtils
import org.minefortress.utils.BlockUtils
import org.minefortress.utils.ModUtils

fun registerPlayerBlockEvents() {
    UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
        if (!world.isBlueprintWorld() || world.getBlockState(hitResult.blockPos).isOf(FortressBlocks.FORTRESS_BUILDING))
            return@register ActionResult.PASS

        val cell = player.getPersonalBlueprintCell()
        if (!cell.contains(hitResult.blockPos))
            return@register ActionResult.FAIL

        return@register ActionResult.PASS
    }

    AttackBlockCallback.EVENT.register { player, world, hand, blockPos, direction ->
        val cell = player.getPersonalBlueprintCell()

        return@register if (!world.isBlueprintWorld())
            ActionResult.PASS
        else if (!cell.contains(blockPos))
            ActionResult.FAIL
        else ActionResult.PASS
    }

    UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
        if (!world.isClient || !ModUtils.isClientInFortressGamemode())
            return@register ActionResult.PASS

        val clientBlueprintManager = ModUtils.getBlueprintManager()
        val fortressManager = ModUtils.getFortressClientManager()

        if (fortressManager.state == FortressState.COMBAT) {
            val influenceManager = ModUtils.getInfluenceManager()
            if (influenceManager.isSelecting) {
                influenceManager.selectInfluencePosition()
                return@register ActionResult.SUCCESS
            }
            updateFightSelection(hitResult, fortressManager)
            return@register ActionResult.SUCCESS
        }

        if (fortressManager.state == FortressState.AREAS_SELECTION) {
            val areasClientManager = ModUtils.getAreasClientManager()
            if (areasClientManager.isSelecting) areasClientManager.resetSelection()
            else areasClientManager.removeHovered()
            return@register ActionResult.SUCCESS
        }

        if (clientBlueprintManager.isSelecting) {
            clientBlueprintManager.buildCurrentStructure()
            return@register ActionResult.SUCCESS
        }

        if (fortressManager.isBuildingHovered) {
            openManageBuildingMenu(fortressManager)
            return@register ActionResult.SUCCESS
        }

        val stackInHand = player.getStackInHand(hand)
        val item = stackInHand.item
        val useoncontext = ItemUsageContext(player, hand, hitResult)
        val blockStateFromItem = BlockUtils.getBlockStateFromItem(item)
        if (blockStateFromItem != null) {
            clickBuild(useoncontext, blockStateFromItem)
            return@register ActionResult.SUCCESS
        }
        val selectionManager = ModUtils.getSelectionManager()
        if (selectionManager.isSelecting) {
            selectionManager.selectBlock(hitResult.blockPos, null)
            return@register ActionResult.SUCCESS
        }

        return@register ActionResult.PASS
    }

}

private fun updateFightSelection(hitResult: BlockHitResult, fortressManager: IClientFortressManager) {
    val fightManager = fortressManager.fightManager
    fightManager.setTarget(hitResult, CoreModUtils.getMineFortressManagersProvider().targetedSelectionManager)
}

private fun openManageBuildingMenu(fortressManager: IClientFortressManager) {
    fortressManager
        .hoveredBuilding
        .ifPresent { it: IEssentialBuildingInfo? -> }
}

private fun clickBuild(useOnContext: ItemUsageContext, blockState: BlockState) {
    var blockPos = useOnContext.blockPos
    if (!BuildingHelper.canPlaceBlock(useOnContext.world, blockPos)) {
        blockPos = blockPos.offset(useOnContext.side)
    }

    ModUtils.getSelectionManager().selectBlock(blockPos, blockState)
}