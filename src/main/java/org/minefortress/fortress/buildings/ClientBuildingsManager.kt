package org.minefortress.fortress.buildings

import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.remmintan.mods.minefortress.core.FortressState.*
import net.remmintan.mods.minefortress.core.dtos.buildings.BuildingHealthRenderInfo
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import net.remmintan.mods.minefortress.core.interfaces.buildings.IClientBuildingsManager
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding
import net.remmintan.mods.minefortress.networking.c2s.C2SOpenBuildingScreen
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper
import org.minefortress.renderer.gui.fortress.RepairBuildingScreen
import org.minefortress.utils.BlockUtils
import org.minefortress.utils.ModUtils
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.math.max

class ClientBuildingsManager : IClientBuildingsManager {

    private var buildings: List<BlockPos> = emptyList()
    private var hoveredBuilding: IFortressBuilding? = null

    override fun updateBuildings(buildings: List<BlockPos>) {
        this.buildings = buildings
    }

    override fun getBuildingSelection(pos: BlockPos?): List<BlockPos> {
        for (buildingPos in buildings) {
            val buildingOpt: Optional<IFortressBuilding> = getBuilding(buildingPos)
            if (buildingOpt.isEmpty) continue
            val building = buildingOpt.get()
            val start = building.start
            val end = building.end
            if (BlockUtils.isPosBetween(pos, start, end)) {
                hoveredBuilding = building
                return StreamSupport
                    .stream(BlockPos.iterate(start, end).spliterator(), false)
                    .map { obj: BlockPos -> obj.toImmutable() }
                    .collect(Collectors.toList())
            }
        }
        hoveredBuilding = null
        return emptyList()
    }

    override fun getHoveredBuilding(): Optional<IFortressBuilding> {
        return Optional.ofNullable(hoveredBuilding)
    }

    override fun countBuildings(type: ProfessionType, level: Int): Int {
        return getBuildingsStream()
            .filter { it.satisfiesRequirement(type, level) }
            .count()
            .toInt()
    }

    private fun getBuilding(pos: BlockPos): Optional<IFortressBuilding> {
        val blockEntity = MinecraftClient.getInstance().world?.getBlockEntity(pos)
        return if (blockEntity is IFortressBuilding) Optional.of(blockEntity) else Optional.empty()
    }

    override fun getBuildingHealths(): List<BuildingHealthRenderInfo> {

        return when (ModUtils.getFortressClientManager().state) {
            COMBAT -> getBuildingsStream()
                .filter { it.health < 100 }
                .map { buildingToHealthRenderInfo(it) }
                .toList()

            BUILD_SELECTION, BUILD_EDITING -> getBuildingsStream()
                .filter { it.health < 33 }
                .map { buildingToHealthRenderInfo(it) }
                .toList()

            else -> emptyList()
        }
    }

    override fun openRepairBuildingScreen(pos: BlockPos, blocksToRepair: Map<BlockPos, BlockState>) {
        val resourceManager = ModUtils.getFortressClientManager().resourceManager
        MinecraftClient.getInstance().setScreen(RepairBuildingScreen(pos, blocksToRepair, resourceManager))
    }

    override fun hasRequiredBuilding(type: ProfessionType, level: Int, minCount: Int): Boolean {
        val requiredBuilding = getBuildingsStream().filter { b -> b.satisfiesRequirement(type, level) }
        if (type == ProfessionType.MINER || type == ProfessionType.LUMBERJACK || type == ProfessionType.WARRIOR) {
            return requiredBuilding
                .mapToLong { it.getBedsCount() * 10L }
                .sum() > minCount
        }
        val count = requiredBuilding.count()

        return when (type) {
            ProfessionType.ARCHER -> count * 10
            ProfessionType.FARMER -> count * 5
            ProfessionType.FISHERMAN -> count * 3
            else -> count
        } > minCount
    }

    override fun openBuildingScreen(playerEntity: PlayerEntity) {
        hoveredBuilding?.pos?.let {
            val packet = C2SOpenBuildingScreen(it)
            FortressClientNetworkHelper.send(C2SOpenBuildingScreen.CHANNEL, packet)
        }
    }

    private fun buildingToHealthRenderInfo(buildingInfo: IFortressBuilding): BuildingHealthRenderInfo {
        val start = buildingInfo.start
        val end = buildingInfo.end

        val maxY = max(start.y.toDouble(), end.y.toDouble()).toInt()
        val centerX = (start.x + end.x) / 2
        val centerZ = (start.z + end.z) / 2

        val center = Vec3d(centerX.toDouble(), maxY.toDouble(), centerZ.toDouble())
        val health = buildingInfo.health

        return BuildingHealthRenderInfo(center, health)
    }

    private fun getBuildingsStream(): Stream<IFortressBuilding> {
        return buildings.stream()
            .map { getBuilding(it) }
            .filter { it.isPresent }
            .map { it.get() }
    }

}