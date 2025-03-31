package net.remmintan.mods.minefortress.networking.c2s

import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.entities.player.IFortressPlayerEntity
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket
import net.remmintan.mods.minefortress.core.utils.getManagersProvider

class C2SSetupCampfirePacket(private val startPos: BlockPos) : FortressC2SPacket {

    constructor(buf: PacketByteBuf) : this(buf.readBlockPos())

    override fun handle(server: MinecraftServer, player: ServerPlayerEntity) {
        val playerManagers = player.getManagersProvider()
        val blueprintManager = playerManagers.get_BlueprintManager()
        val task = blueprintManager.createInstantPlaceTask("campfire", startPos, BlockRotation.NONE)

        val world = server.overworld
        val fortressPos = task.execute(world, player)
        (player as IFortressPlayerEntity).set_FortressPos(fortressPos)
        server.getManagersProvider(fortressPos)?.sync()
    }

    override fun write(buf: PacketByteBuf?) {
        buf?.writeBlockPos(startPos)
    }

    companion object {
        const val CHANNEL = "setup_campfire"
    }
}