package net.remmintan.mods.minefortress.networking.s2c

import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket

class ClientboundSyncContainerPositionsPacket(private val positions: List<BlockPos>) : FortressS2CPacket {

    constructor(buf: PacketByteBuf) : this(buf.readLongArray().map { BlockPos.fromLong(it) })

    override fun handle(client: MinecraftClient) {
        val provider = managersProvider
        val resourceManager = provider._ClientFortressManager.resourceManager
        resourceManager.sync(positions)
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeLongArray(positions.map { it.asLong() }.toLongArray())
    }
}
