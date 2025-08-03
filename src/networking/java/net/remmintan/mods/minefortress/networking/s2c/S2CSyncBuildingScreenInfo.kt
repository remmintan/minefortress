package net.remmintan.mods.minefortress.networking.s2c

import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.remmintan.mods.minefortress.core.dtos.buildings.BuildingScreenInfo
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket
import net.remmintan.mods.minefortress.core.utils.ClientModUtils

class S2CSyncBuildingScreenInfo(private val info: BuildingScreenInfo) : FortressS2CPacket {

    constructor(buf: PacketByteBuf) : this(BuildingScreenInfo.readFromPacketByteBuf(buf))

    override fun write(buf: PacketByteBuf) {
        info.writeToPacketByteBuf(buf)
    }

    override fun handle(client: MinecraftClient) {
        val buildingScreenInfoService = ClientModUtils.getFortressManager().clientBuildingScreenInfoService
        buildingScreenInfoService.syncState(info)
    }

    companion object {
        const val CHANNEL = "fortress_sync_building_screen_info"
    }
}