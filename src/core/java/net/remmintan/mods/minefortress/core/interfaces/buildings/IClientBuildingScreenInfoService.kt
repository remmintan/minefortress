package net.remmintan.mods.minefortress.core.interfaces.buildings

import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.dtos.buildings.BuildingScreenInfo
import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionHireInfo

interface IClientBuildingScreenInfoService {

    fun requestUpdate(buildingPos: BlockPos)
    fun syncState(info: BuildingScreenInfo)
    fun getProfessions(): List<ProfessionHireInfo>
    fun tick(client: MinecraftClient)

}