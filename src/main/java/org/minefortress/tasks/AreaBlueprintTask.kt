package org.minefortress.tasks

import net.minecraft.block.BedBlock
import net.minecraft.block.enums.BedPart
import net.minecraft.entity.LivingEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import net.remmintan.mods.minefortress.core.TaskType
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintDataLayer
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IStructureBlockData
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity
import net.remmintan.mods.minefortress.core.interfaces.tasks.IAreaBasedTask
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo
import net.remmintan.mods.minefortress.core.utils.*
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper
import net.remmintan.mods.minefortress.networking.s2c.ClientboundTaskExecutedPacket
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo
import org.minefortress.tasks.block.info.DigTaskBlockInfo
import java.util.*
import kotlin.math.max
import kotlin.math.sqrt

class AreaBlueprintTask(
    private val metadata: BlueprintMetadata,
    placePos: BlockPos,
    private val blueprintData: IStructureBlockData,
    world: World
) : IAreaBasedTask {
    override val areaData: Pair<BlockPos, Double>
    private val startPos = placePos.down(metadata.floorLevel)
    private val endPos = startPos.add(blueprintData.size)
    private val blocksQueue: Queue<ITaskBlockInfo> = LinkedList()
    private val totalManualBlocks: Int
    private val succeededBlocks: MutableSet<BlockPos> = mutableSetOf()

    private var canceled: Boolean = false
    private var assignedWorkers = 0

    override val positions: List<BlockPos>

    val requiredResources: List<ItemStack> = blueprintData.stacks.map { ItemStack(it.item, it.amount) }

    init {
        val areaBox = BlockBox.create(startPos, endPos)
        areaData = areaBox.center to areaBox.dimensions.len() / 1.5

        val manualLayer = blueprintData.getLayer(BlueprintDataLayer.MANUAL)
        val autoLayer = blueprintData.getLayer(BlueprintDataLayer.AUTOMATIC)
        val entityLayer = blueprintData.getLayer(BlueprintDataLayer.ENTITY)

        val digPositions = (manualLayer + autoLayer + entityLayer)
            .filterValues { !it.isAir }
            .keys
            .map { it.add(startPos) }
            .filter { !BuildingHelper.canPlaceBlock(world, it) }
            .map { DigTaskBlockInfo(it) }
        blocksQueue.addAll(digPositions)

        val preparedBlockInfos = manualLayer
            .map { (p, s) -> BlockStateTaskBlockInfo(s.block.asItem(), p.add(startPos), s) }
        blocksQueue.addAll(preparedBlockInfos)
        totalManualBlocks = preparedBlockInfos.size

        positions = (manualLayer.keys + autoLayer.keys + entityLayer.keys)
            .map { it.add(startPos) }
            .toList()
    }

    override fun canTakeMoreWorkers() = hasMoreBlocks() && (assignedWorkers < max(totalManualBlocks / 10, 1))

    override fun addWorker() {
        assignedWorkers++
    }

    override fun removeWorker() {
        assignedWorkers--
    }

    override fun getNextBlock(): ITaskBlockInfo? {
        return blocksQueue.poll()
    }

    override fun failBlock(info: ITaskBlockInfo) {
        blocksQueue.add(info)
    }

    override fun successBlock(pos: BlockPos) {
        succeededBlocks.add(pos)
    }

    override fun hasMoreBlocks() = blocksQueue.isNotEmpty()

    override fun toTaskInformationDto(): List<TaskInformationDto> {
        return listOf(TaskInformationDto(pos, positions, TaskType.BUILD))
    }

    override fun isComplete() = succeededBlocks.size == totalManualBlocks

    override fun onCompletion(worker: IFortressAwareEntity) {
        val world = (worker as LivingEntity).world

        val entityLayer = blueprintData.getLayer(BlueprintDataLayer.ENTITY)
        val automaticLayer = blueprintData.getLayer(BlueprintDataLayer.AUTOMATIC)

        (entityLayer + automaticLayer)
            .forEach { (p, s) ->
                val realPos = p.add(startPos)
                world.setBlockState(realPos, s)
                if (!s.isIn(BlockTags.BEDS) || s.get(BedBlock.PART) != BedPart.FOOT)
                    removeReservedItem(worker, s.block.asItem())
            }

        val pos = worker.fortressPos ?: error("No fortress pos")
        val server = (worker as IFortressAwareEntity).server

        val manualLayer = blueprintData.getLayer(BlueprintDataLayer.MANUAL)
        server.getManagersProvider(pos)
            .buildingsManager
            ?.addBuilding(metadata, startPos, endPos, (entityLayer + automaticLayer + manualLayer))

        server.getFortressOwner(pos)?.let {
            val packet = ClientboundTaskExecutedPacket(pos)
            FortressServerNetworkHelper.send(it, FortressChannelNames.FINISH_TASK, packet)
        }
    }

    private fun removeReservedItem(worker: IFortressAwareEntity, item: Item) {
        if (worker.server.isSurvivalFortress()) {
            val provider = worker.server.getManagersProvider(worker.fortressPos!!)
            provider.resourceHelper.payItemFromTask(pos, item, SimilarItemsHelper.isIgnorable(item))
        }
    }

    override fun cancel() {
        canceled = true
    }

    override fun notCancelled() = !canceled

    companion object {
        fun Vec3i.len(): Double {
            return sqrt(x.toDouble() * x + y * y + z * z)
        }
    }

}