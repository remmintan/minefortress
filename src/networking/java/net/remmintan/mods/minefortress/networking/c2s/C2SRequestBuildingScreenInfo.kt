package net.remmintan.mods.minefortress.networking.c2s

import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket
import net.remmintan.mods.minefortress.core.utils.LogCompanion

class C2SRequestBuildingScreenInfo(private val buildingPos: BlockPos) : FortressC2SPacket {

    constructor(buf: PacketByteBuf) : this(buf.readBlockPos())

    override fun handle(server: MinecraftServer, player: ServerPlayerEntity) {
        server.overworld.getBlockEntity(buildingPos)?.let {
            if (it is IFortressBuilding) {
                it.constructAndSendBuildingScreenInfo(player)
            } else {
                log.error("Trying to sync building screen info but the entity at pos $buildingPos is not building but ${it::class}")
            }
        } ?: log.error("Trying to sync building screen info but there is no block entity at $buildingPos")
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeBlockPos(buildingPos)
    }

    companion object : LogCompanion(C2SRequestBuildingScreenInfo::class) {
        const val CHANNEL = "fortress_request_building_screen_info"
    }

}