package net.remmintan.mods.minefortress.core.dtos.buildings

import net.minecraft.network.PacketByteBuf
import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionHireInfo

data class BuildingScreenInfo(
    val professions: List<ProfessionHireInfo>
) {
    fun writeToPacketByteBuf(buf: PacketByteBuf) {
        buf.writeCollection(professions) { b, it -> it.writeToPacketByteBuf(b) }
    }

    companion object {

        fun readFromPacketByteBuf(buf: PacketByteBuf): BuildingScreenInfo {
            val professions = buf.readCollection({ mutableListOf<ProfessionHireInfo>() }) {
                ProfessionHireInfo.readFromPacketByteBuf(it)
            }

            return BuildingScreenInfo(professions)
        }

    }
}
