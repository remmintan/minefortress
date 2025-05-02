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
import net.remmintan.gobi.helpers.TreeFinder
import net.remmintan.mods.minefortress.core.interfaces.selections.ClickType
import net.remmintan.mods.minefortress.core.utils.ClientModUtils
import net.remmintan.mods.minefortress.networking.c2s.ServerboundCutTreesTaskPacket
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper
import java.util.*
import kotlin.math.max
import kotlin.math.min

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
            end?.let { prevBox = start!! boxTo it }
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
        val newBox = start!! boxTo end!!
        selectedTrees
            .filter { (_, tree) -> tree.treeLogBlocks.none { newBox.contains(it) } }
            .keys
            .forEach { selectedTrees.remove(it) }

        // adding new trees
        val treeFinder = TreeFinder(world)
        for (pos in start!! iterateTo end!!) {
            if (prevBox?.contains(pos) == true) continue
            if (treeFinder.visited(pos)) continue

            treeFinder.findTree(pos)?.let { tree ->
                selectedTrees[tree.treeRootBlock] = tree
            }
        }
    }

    private fun getWorld(): World {
        return MinecraftClient.getInstance().world ?: error("No world available")
    }

    private infix fun BlockPos.boxTo(other: BlockPos): BlockBox {
        val (newStart, newEnd) = moveStartEnd(this, other)
        return BlockBox.create(newStart, newEnd)
    }

    private infix fun BlockPos.iterateTo(other: BlockPos): List<BlockPos> {
        val (newStart, newEnd) = moveStartEnd(this, other)
        return BlockPos.iterate(newStart, newEnd).map { it.toImmutable() }
    }

    private fun moveStartEnd(start: BlockPos, end: BlockPos): kotlin.Pair<BlockPos, BlockPos> {
        val world = getWorld()

        val newStart = BlockPos(start.x, max(world.bottomY, start.y - 20), start.z)
        val newEnd = BlockPos(end.x, min(world.topY, end.y + 20), end.z)
        return kotlin.Pair(newStart, newEnd)
    }

}
