package org.minefortress.tasks

import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.remmintan.mods.minefortress.core.TaskType
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintDataLayer
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IStructureBlockData
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity
import net.remmintan.mods.minefortress.core.interfaces.tasks.IAreaBasedTask
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo
import net.remmintan.mods.minefortress.core.utils.getFortressOwner
import net.remmintan.mods.minefortress.core.utils.getManagersProvider
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper
import net.remmintan.mods.minefortress.networking.s2c.ClientboundTaskExecutedPacket
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo
import java.util.*
import kotlin.math.sqrt

class AreaBlueprintTask(
    private val id: UUID,
    private val metadata: BlueprintMetadata,
    private val startPos: BlockPos,
    private val blueprintData: IStructureBlockData,
) : IAreaBasedTask {
    override val areaData: Pair<BlockPos, Double>
    private val blocksQueue: Queue<ITaskBlockInfo> = LinkedList()
    private val totalManualBlocks: Int
    private val succeededBlocks: MutableSet<BlockPos> = mutableSetOf()
    private var canceled: Boolean = false
    private val endPos: BlockPos

    init {
        endPos = startPos.down(metadata.floorLevel).add(blueprintData.size)
        val areaBox = BlockBox.create(startPos, endPos)

        areaData = areaBox.center to areaBox.dimensions.len() / 1.5


        val preparedBlockInfos = blueprintData.getLayer(BlueprintDataLayer.MANUAL)
            .map { (p, s) -> BlockStateTaskBlockInfo(s.block.asItem(), p.add(startPos), s) }
        blocksQueue.addAll(preparedBlockInfos)
        totalManualBlocks = preparedBlockInfos.size
    }

    override fun getId() = id

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
        val manualLayer = blueprintData.getLayer(BlueprintDataLayer.MANUAL)
        val autoLayer = blueprintData.getLayer(BlueprintDataLayer.AUTOMATIC)
        val entityLayer = blueprintData.getLayer(BlueprintDataLayer.ENTITY)

        val positions = (manualLayer.keys + autoLayer.keys + entityLayer.keys)
            .map { it.add(startPos) }
            .toList()

        return listOf(TaskInformationDto(id, positions, TaskType.BUILD))
    }

    override fun isComplete() = succeededBlocks.size == totalManualBlocks

    override fun onCompletion(pawn: IFortressAwareEntity) {
        val world = (pawn as LivingEntity).world

        val entityLayer = blueprintData.getLayer(BlueprintDataLayer.ENTITY)
        val automaticLayer = blueprintData.getLayer(BlueprintDataLayer.AUTOMATIC)

        (entityLayer + automaticLayer)
            .forEach { (p, s) ->
                val realPos = p.add(startPos)
                world.setBlockState(realPos, s)
            }

        val pos = pawn.fortressPos ?: error("No fortress pos")
        val server = (pawn as IFortressAwareEntity).server

        val manualLayer = blueprintData.getLayer(BlueprintDataLayer.MANUAL)
        server.getManagersProvider(pos)
            ?.buildingsManager
            ?.addBuilding(metadata, startPos, endPos, (entityLayer + automaticLayer + manualLayer))

        server.getFortressOwner(pos)?.let {
            val packet = ClientboundTaskExecutedPacket(this.id)
            FortressServerNetworkHelper.send(it, FortressChannelNames.FINISH_TASK, packet)
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