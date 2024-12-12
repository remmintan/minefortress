package net.remmintan.mods.minefortress.core.interfaces.buildings

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.dtos.buildings.BuildingHealthRenderInfo
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import java.util.*

interface IClientBuildingsManager {

    fun updateBuildings(buildings: List<BlockPos>)

    fun getBuildingSelection(pos: BlockPos?): List<BlockPos>

    fun isBuildingHovered(): Boolean {
        return getHoveredBuilding().isPresent
    }

    fun getHoveredBuildingName(): Optional<String> {
        return getHoveredBuilding().map { obj: IFortressBuilding -> obj.name }
    }

    fun getHoveredBuilding(): Optional<IFortressBuilding>

    fun countBuildings(type: ProfessionType, level: Int): Int

    fun getBuildingHealths(): List<BuildingHealthRenderInfo>

    fun hasRequiredBuilding(type: ProfessionType, level: Int, minCount: Int): Boolean

    fun openBuildingScreen(playerEntity: PlayerEntity)

}