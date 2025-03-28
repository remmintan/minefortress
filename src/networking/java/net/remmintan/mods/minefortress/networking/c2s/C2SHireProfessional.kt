package net.remmintan.mods.minefortress.networking.c2s

import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket

class C2SHireProfessional(private val pos: BlockPos, private val professionId: String) : FortressC2SPacket {

    companion object {
        const val CHANNEL = "hire_professional"
    }

    constructor(buf: PacketByteBuf) : this(buf.readBlockPos(), buf.readString())

    override fun write(buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
        buf.writeString(professionId)
    }

    override fun handle(server: MinecraftServer, player: ServerPlayerEntity) {
        (player.world?.getBlockEntity(pos) as? IFortressBuilding)?.hireHandler?.hire(professionId)
    }
}