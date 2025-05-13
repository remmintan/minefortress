package org.minefortress.entity.ai.goal

import net.remmintan.mods.minefortress.core.TaskType
import org.minefortress.entity.Colonist

class PawnExecuteAreaBasedTaskGoal(colonist: Colonist) : AbstractFortressGoal(colonist) {


    override fun canStart(): Boolean {
        return colonist.areaBasedTaskControl.hasMoreBlocks() && !isHungry
    }

    override fun start() {
        colonist.resetControls()
        colonist.isAllowToPlaceBlockFromFarAway = true
        if (colonist.isSleeping) {
            colonist.wakeUp()
        }
    }

    override fun tick() {
        val haveSomethingToWorkOn =
            colonist.areaBasedTaskControl.hasMoreBlocks() || colonist.areaBasedTaskControl.getCurrentBlock() != null
        if (!haveSomethingToWorkOn) return

        val areaBasedTaskControl = colonist.areaBasedTaskControl
        if (!areaBasedTaskControl.isWithinTheArea()) {
            // try to come to work area
            val (center, r) = areaBasedTaskControl.getAreaData()
            val movementHelper = colonist.movementHelper
            if (movementHelper.goal != center) {
                movementHelper.goTo(center, reachDistance = r)
            }

            if (!movementHelper.hasReachedGoal() && movementHelper.isStuck) {
                TODO("Fail the task, can't reach the area")
            }
        } else {
            val currentBlock = colonist.areaBasedTaskControl.getCurrentBlock()
            if (currentBlock == null) {
                moveToNextGoal()
            } else {
                val digSuccess = currentBlock.type == TaskType.REMOVE && colonist.digControl.isDone
                val placeSuccess = currentBlock.type == TaskType.BUILD && colonist.placeControl.isDone
                if (digSuccess || placeSuccess) {
                    moveToNextGoal()
                }
            }
        }
    }

    private fun moveToNextGoal() {
        colonist.areaBasedTaskControl.moveToNextBlock()?.also {
            colonist.setGoal(it)
        }
    }

    override fun shouldContinue(): Boolean {
        return canStart() || colonist.areaBasedTaskControl.getCurrentBlock() != null
    }

    override fun stop() {
        colonist.isAllowToPlaceBlockFromFarAway = false
        colonist.areaBasedTaskControl.reset()
        colonist.resetControls()
    }
}