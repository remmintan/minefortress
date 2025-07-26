package net.remmintan.mods.minefortress.networking.s2c

import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksInformationHolder

class ClientboundTaskExecutedPacket(private val pos: BlockPos) : FortressS2CPacket {

    constructor(buffer: PacketByteBuf) : this(buffer.readBlockPos())

    override fun write(buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
    }

    override fun handle(client: MinecraftClient) {
        val world = client.world as ITasksInformationHolder?
//        world?._ClientTasksHolder?.removeTask(pos)
        TODO()
    }
}
