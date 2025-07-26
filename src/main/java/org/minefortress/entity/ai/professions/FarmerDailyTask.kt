package org.minefortress.entity.ai.professions

import net.minecraft.block.Blocks
import net.minecraft.block.CropBlock
import net.minecraft.item.BlockItem
import net.minecraft.item.Items
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.FluidTags
import net.minecraft.util.Hand
import net.minecraft.world.event.GameEvent
import net.remmintan.mods.minefortress.core.interfaces.automation.area.AutomationActionType
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationBlockInfo
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import net.remmintan.mods.minefortress.core.utils.getManagersProvider
import net.remmintan.mods.minefortress.core.utils.isCreativeFortress
import org.minefortress.entity.Colonist
import org.minefortress.fortress.automation.iterators.FarmAreaIterator
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo
import org.minefortress.tasks.block.info.DigTaskBlockInfo
import java.util.*

class FarmerDailyTask : AbstractAutomationAreaTask() {
    private var goal: IAutomationBlockInfo? = null

    override fun start(colonist: Colonist) {
        super.start(colonist)
        colonist.baritone.settings().allowParkour.set(false)
    }

    override fun tick(colonist: Colonist) {
        if (this.area == null) return

        val movementHelper = colonist.movementHelper
        if (this.goal == null) {
            do {
                if (!iterator.hasNext()) return
                this.goal = iterator.next()
            } while (goalAlreadyInCorrectState(colonist))
            movementHelper.goTo(goal!!.pos().up())
        }
        if (this.goal != null && movementHelper.goal == null) {
            movementHelper.goTo(goal!!.pos().up())
        }

        if (movementHelper.hasReachedGoal() && colonist.placeControl.isDone && colonist.digControl.isDone) {
            when (goal!!.info()) {
                AutomationActionType.FARM_CROPS -> doFarmCrops(colonist)
                AutomationActionType.FARM_WATER -> doSetWater(colonist)
                else -> error("Wrong action type for Farm area")
            }
        }

        if (movementHelper.goal != null && !movementHelper.hasReachedGoal() && movementHelper.isStuck) {
            val workGoal = movementHelper.goal!!.up()
            colonist.teleport(workGoal.x.toDouble(), workGoal.y.toDouble(), workGoal.z.toDouble())
        }
    }

    private fun doFarmCrops(colonist: Colonist) {
        val movementHelper = colonist.movementHelper
        val goalBlockState = colonist.world.getBlockState(goal!!.pos())
        val aboveBlockPos = goal!!.pos().up()
        val aboveBlockState = colonist.world.getBlockState(aboveBlockPos)
        if (goalBlockState.isOf(Blocks.DIRT) || goalBlockState.isOf(Blocks.GRASS_BLOCK)) {
            if (FarmAreaIterator.blockCanBeRemovedToPlantCrops(aboveBlockState)) {
                colonist.setGoal(DigTaskBlockInfo(aboveBlockPos))
            } else {
                colonist.putItemInHand(Items.WOODEN_HOE)
                colonist.swingHand(Hand.MAIN_HAND)
                colonist.world.setBlockState(goal!!.pos(), Blocks.FARMLAND.defaultState, 3)
                colonist.world.emitGameEvent(colonist, GameEvent.BLOCK_PLACE, goal!!.pos())
            }
        } else if (goalBlockState.isOf(Blocks.FARMLAND)) {
            val cropBlock = aboveBlockState.block
            if (aboveBlockState.isIn(BlockTags.CROPS) && cropBlock is CropBlock) {
                if (cropBlock.getAge(aboveBlockState) == cropBlock.maxAge) {
                    val digTaskBlockInfo = DigTaskBlockInfo(aboveBlockPos)
                    colonist.setGoal(digTaskBlockInfo)
                } else {
                    this.goal = null
                }
            } else if (aboveBlockState.isAir) {
                val seedsOpt = getSeeds(colonist)
                if (seedsOpt.isPresent) {
                    val blockItem = seedsOpt.get()
                    val bsTaskBlockInfo =
                        BlockStateTaskBlockInfo(blockItem, aboveBlockPos, blockItem.block.defaultState)
                    colonist.setGoal(bsTaskBlockInfo)
                    movementHelper.goTo(aboveBlockPos)
                } else {
                    this.goal = null
                }
            } else if (FarmAreaIterator.blockCanBeRemovedToPlantCrops(aboveBlockState)) {
                colonist.setGoal(DigTaskBlockInfo(aboveBlockPos))
            } else {
                this.goal = null
            }
        } else {
            this.goal = null
        }
    }

    private fun doSetWater(colonist: Colonist) {
        val goalBlockState = colonist.world.getBlockState(goal!!.pos())
        if (goalBlockState.isIn(BlockTags.SHOVEL_MINEABLE)) {
            colonist.setGoal(DigTaskBlockInfo(goal!!.pos()))
        } else if (goalBlockState.isAir) {
            colonist.putItemInHand(Items.WATER_BUCKET)
            colonist.swingHand(Hand.MAIN_HAND)
            colonist.world.setBlockState(goal!!.pos(), Blocks.WATER.defaultState, 3)
            colonist.world.emitGameEvent(colonist, GameEvent.BLOCK_PLACE, goal!!.pos())
        } else {
            this.goal = null
        }
    }

    private fun goalAlreadyInCorrectState(colonist: Colonist): Boolean {
        val goalBlockState = colonist.world.getBlockState(goal!!.pos())
        val abovePos = goal!!.pos().up()
        val aboveBlockState = colonist.world.getBlockState(abovePos)

        if (goal!!.info() == AutomationActionType.FARM_CROPS) {
            val aboveBlock = aboveBlockState.block
            return goalBlockState.isOf(Blocks.FARMLAND)
                    && aboveBlockState.isIn(BlockTags.CROPS)
                    && aboveBlock is CropBlock
                    && aboveBlock.getAge(aboveBlockState) < aboveBlock.maxAge
        }

        if (goal!!.info() == AutomationActionType.FARM_WATER) {
            return goalBlockState.fluidState.isIn(FluidTags.WATER)
        }

        return false
    }

    override fun shouldContinue(colonist: Colonist): Boolean {
        return colonist.world.isDay && (iterator.hasNext() || this.goal != null)
    }

    override fun getProfessionType(): ProfessionType {
        return ProfessionType.FARMER
    }

    override fun getTaskDesc(): String {
        return "Farming"
    }

    private fun getSeeds(colonist: Colonist): Optional<BlockItem> {
        if (colonist.server.isCreativeFortress()) {
            return Optional.of(Items.WHEAT_SEEDS as BlockItem)
        }

        val managersProvider = colonist.server.getManagersProvider(colonist.fortressPos!!)
        return Optional.ofNullable(managersProvider.foodManager.getFarmerSeeds() as? BlockItem)
    }
}
