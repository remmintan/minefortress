package net.remmintan.mods.minefortress.networking.c2s

import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket

class C2SDestroyBuilding(private val pos: BlockPos) : FortressC2SPacket {

    constructor(buf: PacketByteBuf) : this(BlockPos.fromLong(buf.readLong()))

    override fun handle(server: MinecraftServer, player: ServerPlayerEntity) {
        getManagersProvider(server, player).buildingsManager.destroyBuilding(pos)
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeLong(pos.asLong())
    }

    companion object {
        const val CHANNEL: String = "destroy_building"
    }
}
