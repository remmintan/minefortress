package net.remmintan.mods.minefortress.networking.c2s

import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket

class C2SOpenBuildingHireScreen(val profId: String) : FortressC2SPacket {

    companion object {
        const val CHANNEL = "open_building_hire_screen"
    }

    constructor(buf: PacketByteBuf) : this(buf.readString())

    override fun write(buf: PacketByteBuf) {
        buf.writeString(profId)
    }

    override fun handle(server: MinecraftServer?, player: ServerPlayerEntity?) {
        val provider = getManagersProvider(server, player)
        val manager = provider.professionsManager

        val profession = manager.getProfession(profId)
        val requirementType = profession.getRequirementType()
        val requirementLevel = profession.getRequirementLevel()
        val buildings = provider.buildingsManager.getBuildings(requirementType, requirementLevel)
        if (buildings.isNotEmpty()) {
            val building = buildings[0]
            if (building is NamedScreenHandlerFactory) {
                player!!.openHandledScreen(building)
                //player.currentScreenHandler.
                // TODO tell screen handler or building to open hire menu
            }
        }
    }
}