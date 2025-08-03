package org.minefortress.fortress.buildings

import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.dtos.buildings.BuildingScreenInfo
import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionHireInfo
import net.remmintan.mods.minefortress.core.interfaces.buildings.IClientBuildingScreenInfoService
import net.remmintan.mods.minefortress.gui.building.BuildingScreenHandler
import net.remmintan.mods.minefortress.networking.c2s.C2SRequestBuildingScreenInfo
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper
import org.minefortress.events.BuildingScreenInfoSyncedCallback

class ClientBuildingScreenInfoService : IClientBuildingScreenInfoService {

    private var refreshCooldown: Int = 0
        set(value) {
            field = value.coerceAtLeast(0)
        }
    private var buildingScreenInfo: BuildingScreenInfo? = null

    override fun requestUpdate(buildingPos: BlockPos) {
        val packet = C2SRequestBuildingScreenInfo(buildingPos)
        FortressClientNetworkHelper.send(C2SRequestBuildingScreenInfo.CHANNEL, packet)
    }

    override fun syncState(info: BuildingScreenInfo) {
        this.buildingScreenInfo = info.copy()
        BuildingScreenInfoSyncedCallback.EVENT.invoker().buildingScreenInfoSynced()
        refreshCooldown = 10
    }

    override fun getProfessions(): List<ProfessionHireInfo> {
        return buildingScreenInfo?.professions ?: emptyList()
    }

    override fun tick(client: MinecraftClient) {
        refreshCooldown--
        val currentScreenHandler = client.player?.currentScreenHandler
        if (currentScreenHandler is BuildingScreenHandler && refreshCooldown == 0) {
            requestUpdate(currentScreenHandler.getBuildingPos())
        }
    }
}
