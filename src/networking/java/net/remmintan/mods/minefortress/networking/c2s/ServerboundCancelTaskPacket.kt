package net.remmintan.mods.minefortress.networking.c2s

import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket

class ServerboundCancelTaskPacket(private val taskPos: BlockPos) : FortressC2SPacket {

    constructor(buf: PacketByteBuf) : this(buf.readBlockPos())

    override fun write(buf: PacketByteBuf) {
        buf.writeBlockPos(taskPos)
    }

    override fun handle(server: MinecraftServer, player: ServerPlayerEntity) {
        val provider = getManagersProvider(player)
        val taskManager = provider.taskManager
        taskManager.cancelTask(pos = taskPos, player = player)
    }
}
