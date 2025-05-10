package org.minefortress.tasks

import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.remmintan.mods.minefortress.core.TaskType
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintDataLayer
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IStructureBlockData
import net.remmintan.mods.minefortress.core.interfaces.tasks.IAreaBasedTask
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo
import java.util.*
import kotlin.math.sqrt

class AreaBlueprintTask(
    private val id: UUID,
    metadata: BlueprintMetadata,
    startPos: BlockPos,
    private val blueprintData: IStructureBlockData,
) : IAreaBasedTask {
    override val areaData: Pair<BlockPos, Double>
    private val blocksQueue: Queue<ITaskBlockInfo> = LinkedList()
    private val totalManualBlocks: Int
    private val succeededBlocks: MutableSet<BlockPos> = mutableSetOf()
    private var canceled: Boolean = false

    init {
        val endPos = startPos.down(metadata.floorLevel).add(blueprintData.size)
        val areaBox = BlockBox.create(startPos, endPos)

        areaData = areaBox.center to areaBox.dimensions.len()


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

        val positions = manualLayer.keys + autoLayer.keys + entityLayer.keys

        return listOf(TaskInformationDto(id, positions.toList(), TaskType.BUILD))
    }

    override fun isComplete() = succeededBlocks.size == totalManualBlocks

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