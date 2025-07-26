package org.minefortress.entity.ai.controls

import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.world.event.GameEvent
import net.remmintan.mods.minefortress.core.utils.BuildingHelper
import net.remmintan.mods.minefortress.core.utils.SimilarItemsHelper
import net.remmintan.mods.minefortress.core.utils.getManagersProvider
import net.remmintan.mods.minefortress.core.utils.isCreativeFortress
import org.minefortress.entity.Colonist
import org.minefortress.entity.colonist.IFortressHungerManager
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo
import org.minefortress.tasks.block.info.ItemTaskBlockInfo
import org.minefortress.tasks.block.info.ReplaceTaskBlockInfo

class PlaceControl(private val colonist: Colonist) : PositionedActionControl() {
    private var placeCooldown = 0f
    private var failedInteractions = 0
    var isCantPlaceUnderMyself: Boolean = false
        private set

    override fun tick() {
        if (isDone) return
        if (!super.canReachTheGoal(colonist) || !colonist.navigation.isIdle) return

        if (placeCooldown > 0) placeCooldown -= 1f / colonist.hungerMultiplier

        if (colonist.blockPos == goal) {
            if (!colonist.isWallAboveTheHead)
                colonist.jumpControl.setActive()
            else
                isCantPlaceUnderMyself = true
        } else {
            if (taskBlockInfo is ReplaceTaskBlockInfo && !BuildingHelper.canPlaceBlock(colonist.world, goal))
                return // waiting until we remove the block
            placeBlock()
        }
    }

    override fun reset() {
        super.reset()
        failedInteractions = 0
        isCantPlaceUnderMyself = false
    }

    private fun placeBlock() {
        colonist.lookAtGoal()
        colonist.putItemInHand(item)

        if (placeCooldown <= 0) {
            colonist.swingHand(Hand.MAIN_HAND)
            colonist.addHunger(IFortressHungerManager.ACTIVE_EXHAUSTION)

            val blockInfo = taskBlockInfo
            if (blockInfo is ItemTaskBlockInfo) place(blockInfo)
            if (blockInfo is BlockStateTaskBlockInfo) place(blockInfo)
            if (blockInfo is ReplaceTaskBlockInfo) place(blockInfo)
        }
    }

    private fun place(blockInfo: ItemTaskBlockInfo) {
        val context = blockInfo.context
        val interactionResult = item.useOnBlock(context)

        if (interactionResult == ActionResult.CONSUME || failedInteractions > 15) {
            if (interactionResult == ActionResult.CONSUME) decreaseResourcesAndAddSpecialBlocksAmount()
            this.reset()
            this.placeCooldown = 6f
        } else {
            failedInteractions++
        }
    }

    private fun place(blockInfo: BlockStateTaskBlockInfo) {
        val stateForPlacement = blockInfo.state

        colonist.world.setBlockState(goal, stateForPlacement, 3)
        colonist.world.emitGameEvent(colonist, GameEvent.BLOCK_PLACE, goal)

        decreaseResourcesAndAddSpecialBlocksAmount()

        this.reset()
        this.placeCooldown = 6f
    }

    private fun place(blockInfo: ReplaceTaskBlockInfo) {
        val stateForPlacement = blockInfo.state

        colonist.world.setBlockState(goal, stateForPlacement, 3)
        colonist.world.emitGameEvent(colonist, GameEvent.BLOCK_PLACE, goal)

        decreaseResourcesAndAddSpecialBlocksAmount()

        this.reset()
        this.placeCooldown = 6f
    }

    private fun decreaseResourcesAndAddSpecialBlocksAmount() {
        val taskControl = colonist.taskControl
        if (colonist.server.isCreativeFortress()) {
            val taskPos = taskControl.currentTaskPos ?: return
            val fortressPos = colonist.fortressPos ?: return
            val rh = colonist.server.getManagersProvider(fortressPos).resourceHelper
            rh.payItemFromTask(taskPos, item, SimilarItemsHelper.isIgnorable(item))
        }
    }
}
