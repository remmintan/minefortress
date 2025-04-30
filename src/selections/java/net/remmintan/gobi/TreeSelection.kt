package net.remmintan.gobi

import com.mojang.datafixers.util.Pair
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.item.Item
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import net.remmintan.gobi.helpers.TreeData
import net.remmintan.gobi.helpers.findTree
import net.remmintan.mods.minefortress.core.interfaces.selections.ClickType
import net.remmintan.mods.minefortress.core.utils.ClientModUtils
import net.remmintan.mods.minefortress.networking.c2s.ServerboundCutTreesTaskPacket
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper
import java.util.*

class TreeSelection : Selection() {
    private var start: BlockPos? = null
    private var end: BlockPos? = null

    private var prevBox: BlockBox? = null

    private val selectedTrees = mutableMapOf<BlockPos, TreeData>()

    override fun isSelecting(): Boolean {
        return start != null
    }

    override fun needUpdate(pickedBlock: BlockPos, upDelta: Int): Boolean {
        return start != null && pickedBlock != end
    }

    override fun selectBlock(
        level: World,
        mainHandItem: Item,
        pickedBlock: BlockPos,
        upDelta: Int,
        click: ClickType,
        clientPacketListener: ClientPlayNetworkHandler,
        hitResult: HitResult
    ): Boolean {
        if (click == ClickType.BUILD) {
            return start != null
        }

        if (start == null) {
            start = pickedBlock
            return false
        } else {
            if (selectedTrees.isNotEmpty()) {
                val newTaskId = UUID.randomUUID()
                val pawnsSelectionManager = ClientModUtils.getPawnsSelectionManager()
                val selectedPawnsIds = pawnsSelectionManager.selectedPawnsIds
                val packet = ServerboundCutTreesTaskPacket(newTaskId, selectedTrees.keys.toList(), selectedPawnsIds)
                FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_CUT_TREES_TASK, packet)
                pawnsSelectionManager.resetSelection()
            }

            return true
        }
    }

    override fun update(pickedBlock: BlockPos, upDelta: Int) {
        if (start != null) {
            end?.let { prevBox = BlockBox.create(start, it) }
            end = pickedBlock.up(upDelta)
            updateSelection()
        }
    }

    override fun getSelection(): List<BlockPos> {
        return selectedTrees.values.flatMap { listOf(it.treeLogBlocks, it.treeLeavesBlocks).flatten() }
    }

    override fun reset() {
        start = null
        selectedTrees.clear()
    }

    override fun getSelectionDimensions(): List<Pair<Vec3i, Vec3i>> = emptyList()

    private fun updateSelection() {
        val world = getWorld()

        // removing old trees
        val newBox = BlockBox.create(start!!, end!!)
        selectedTrees
            .filter { (_, tree) -> tree.treeLogBlocks.none { newBox.contains(it) } }
            .keys
            .forEach { selectedTrees.remove(it) }

        // adding new trees
        val visitedBlocks = mutableSetOf<BlockPos>()
        for (pos in BlockPos.iterate(start, end).map { it.toImmutable() }) {
            if (prevBox?.contains(pos) == true) continue
            if (visitedBlocks.contains(pos)) continue

            findTree(pos, world, visitedBlocks)?.let { tree ->
                selectedTrees[tree.treeRootBlock] = tree
            }
        }
    }

    private fun getWorld(): World {
        return MinecraftClient.getInstance().world ?: error("No world available")
    }

}
