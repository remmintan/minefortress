package org.minefortress.entity.ai.professions

import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.world.ServerWorld
import net.remmintan.gobi.helpers.TreeData
import net.remmintan.gobi.helpers.TreeFinder
import net.remmintan.gobi.helpers.TreeRemover
import net.remmintan.mods.minefortress.core.interfaces.automation.area.AutomationActionType
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationBlockInfo
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider
import net.remmintan.mods.minefortress.core.utils.ServerModUtils
import net.remmintan.mods.minefortress.core.utils.getManagersProvider
import net.remmintan.mods.minefortress.core.utils.isCreativeFortress
import org.minefortress.entity.Colonist
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo
import org.minefortress.tasks.block.info.DigTaskBlockInfo

class LumberjackDailyTask : AbstractAutomationAreaTask() {
    private var goal: IAutomationBlockInfo? = null
    private var tree: TreeData? = null

    override fun getProfessionType(): ProfessionType {
        return ProfessionType.LUMBERJACK
    }

    override fun getTaskDesc(): String {
        return "Harvesting trees"
    }

    override fun tick(colonist: Colonist) {
        if (area == null) return
        val movementHelper = colonist.movementHelper

        if (goal == null) {
            if (!iterator.hasNext()) return
            goal = iterator.next()
        }

        if (goal != null && movementHelper.goal == null) {
            movementHelper.goTo(goal!!.pos().up())
        }

        if (movementHelper.hasReachedGoal() && colonist.placeControl.isDone && colonist.digControl.isDone) {
            val actionType = goal!!.info()
            if (actionType == AutomationActionType.CHOP_TREE) {
                doChopTree(colonist)
            } else if (actionType == AutomationActionType.PLANT_SAPLING) {
                val pos = goal!!.pos()
                val world = colonist.world
                val blockState = world.getBlockState(pos)
                if (blockState.isIn(BlockTags.SAPLINGS)) {
                    this.goal = null
                    colonist.movementHelper.reset()
                } else {
                    (getSapling(colonist) as? BlockItem)?.let {
                        colonist.setGoal(BlockStateTaskBlockInfo(it, pos))
                    }
                }
            } else {
                throw IllegalStateException("Unknown action type: $actionType")
            }
        }


        if (movementHelper.goal != null && !movementHelper.hasReachedGoal() && movementHelper.isStuck) {
            val workGoal = movementHelper.goal!!.up().west()
            colonist.resetControls()
            colonist.teleport(workGoal.x + 0.5, workGoal.y.toDouble(), workGoal.z + 0.5)
        }
    }

    private fun doChopTree(colonist: Colonist) {
        val pos = goal!!.pos()
        val world = colonist.world
        val blockState = world.getBlockState(pos)
        if (blockState.isIn(BlockTags.AXE_MINEABLE)) {
            this.tree = TreeFinder(world, emptySet()).findTree(pos)
            colonist.setGoal(DigTaskBlockInfo(pos))
        } else {
            if (tree == null) {
                this.goal = null
                colonist.movementHelper.reset()
            } else {
                ServerModUtils.getManagersProvider(colonist)
                    .ifPresent { it: IServerManagersProvider ->
                        TreeRemover(world as ServerWorld, it.resourceHelper, colonist).removeTheTree(
                            tree!!
                        )
                    }

                tree = null
            }
        }
    }

    private fun getSapling(colonist: Colonist): Item? {
        if (colonist.server.isCreativeFortress()) {
            return Items.OAK_SAPLING
        } else {
            val managersProvider = colonist.server.getManagersProvider(colonist.fortressPos!!)
            val resourceManager = managersProvider.resourceManager
            val resourceHelper = managersProvider.resourceHelper

            if (resourceHelper.payItems(resourceManager.getStorage(), listOf(Items.OAK_SAPLING.defaultStack)))
                return Items.OAK_SAPLING
            else
                return null
        }
    }

    override fun shouldContinue(colonist: Colonist): Boolean {
        return colonist.world.isDay && (iterator.hasNext() || this.goal != null)
    }

    override fun stop(colonist: Colonist) {
        super.stop(colonist)
        this.goal = null
    }
}
