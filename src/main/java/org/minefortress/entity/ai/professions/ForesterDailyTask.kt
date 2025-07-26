package org.minefortress.entity.ai.professions

import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.utils.BuildingHelper
import net.remmintan.mods.minefortress.core.utils.getManagersProvider
import org.minefortress.entity.Colonist
import org.minefortress.entity.colonist.IFortressHungerManager
import org.minefortress.professions.ProfessionManager

class ForesterDailyTask : ProfessionDailyTask {
    private var goal: BlockPos? = null
    private var workingTicks = 0
    private var interactionsCount = 0
    private var stopTime: Long = 0

    override fun canStart(colonist: Colonist): Boolean {
        return colonist.world.isDay && colonist.world.time - this.stopTime > 400
    }

    override fun start(colonist: Colonist) {
        colonist.currentTaskDesc = "Looking for food"
        this.setGoal(colonist)
        colonist.movementHelper.goTo(goal!!)
    }

    override fun tick(colonist: Colonist) {
        if (this.goal == null) return
        val movementHelper = colonist.movementHelper
        if (movementHelper.hasReachedGoal()) {
            if (workingTicks % 10 * colonist.hungerMultiplier == 0f) {
                colonist.swingHand(if (colonist.world.random.nextFloat() < 0.5f) Hand.MAIN_HAND else Hand.OFF_HAND)
                colonist.putItemInHand(Items.WOODEN_HOE)
                colonist.addHunger(IFortressHungerManager.PASSIVE_EXHAUSTION)
                interactionsCount++
                this.gatherItemAndAddToInventory(colonist)
                if (this.interactionsCount > 2) {
                    this.setGoal(colonist)
                    colonist.movementHelper.goTo(goal!!)
                    this.interactionsCount = 0
                }
            }
            colonist.lookAt(goal)
            workingTicks++
        }

        if (!movementHelper.hasReachedGoal() && movementHelper.isStuck) colonist.teleport(
            goal!!.x.toDouble(),
            goal!!.y.toDouble(), goal!!.z.toDouble()
        )
    }

    override fun stop(colonist: Colonist) {
        this.workingTicks = 0
        this.interactionsCount = 0
        this.stopTime = colonist.world.time
    }

    override fun shouldContinue(colonist: Colonist): Boolean {
        return this.goal != null && this.workingTicks < 200
    }

    private fun gatherItemAndAddToInventory(colonist: Colonist) {
        if (isSuccess(colonist)) {
            val item = getRandomForesterItem(colonist)
            val managersProvider = colonist.server.getManagersProvider(colonist.fortressPos!!)
            managersProvider.resourceHelper.putItemToSuitableContainer(item.defaultStack)
        }
    }

    private fun getRandomForesterItem(colonist: Colonist): Item {
        val random = colonist.world.random
        return ProfessionManager.FORESTER_ITEMS[random.nextInt(ProfessionManager.FORESTER_ITEMS.size)]
    }

    private fun isSuccess(colonist: Colonist): Boolean {
        val random = colonist.world.random
        return random.nextInt(100) < 18
    }

    private fun setGoal(colonist: Colonist) {
        val world = colonist.world

        val fortressCenter = colonist.fortressPos

        val horizontalRange = 10
        val randPointAroundCenter = BlockPos.iterateRandomly(world.random, 1, fortressCenter, horizontalRange)
            .iterator()
            .next()

        this.goal = BlockPos
            .findClosest(
                randPointAroundCenter,
                horizontalRange,
                horizontalRange
            ) { pos: BlockPos? ->
                world.getBlockState(pos).isOf(Blocks.GRASS) || world.getBlockState(pos).isOf(Blocks.TALL_GRASS)
            }.or {
                BlockPos
                    .findClosest(
                        randPointAroundCenter,
                        horizontalRange,
                        horizontalRange
                    ) { pos: BlockPos? -> world.getBlockState(pos).isOf(Blocks.GRASS_BLOCK) }
            }.or {
                BlockPos
                    .findClosest(
                        randPointAroundCenter,
                        horizontalRange,
                        horizontalRange
                    ) { pos: BlockPos? -> BuildingHelper.canStayOnBlock(world, pos) }
                    .map { obj: BlockPos -> obj.up() }
            }
            .orElseGet { colonist.blockPos }
    }
}
