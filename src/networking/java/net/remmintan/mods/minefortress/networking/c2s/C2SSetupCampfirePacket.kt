package net.remmintan.mods.minefortress.networking.c2s

import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket
import net.remmintan.mods.minefortress.core.utils.ServerModUtils
import net.remmintan.mods.minefortress.core.utils.getManagersProvider
import net.remmintan.mods.minefortress.core.utils.setFortressPos

class C2SSetupCampfirePacket(private val startPos: BlockPos) : FortressC2SPacket {

    constructor(buf: PacketByteBuf) : this(buf.readBlockPos())

    override fun handle(server: MinecraftServer, player: ServerPlayerEntity) {
        val playerManagers = player.getManagersProvider()
        val blueprintManager = playerManagers.get_BlueprintManager()
        val task = blueprintManager.createInstantPlaceTask("campfire", startPos, BlockRotation.NONE)

        val world = server.overworld
        val center = BlockBox.create(task.start, task.end)
            .center
            .let {
                val y = world.getTopY(Heightmap.Type.WORLD_SURFACE, it.x, it.y)
                BlockPos(it.x, y, it.z)
            }

        playerManagers.get_FortressCenterSetupManager().setupCenter(world, center)
        player.setFortressPos(center)

        val provider = ServerModUtils.getManagersProvider(server, center).orElseThrow()
        task.addFinishListener {
            ServerModUtils.getFortressManager(server, center).ifPresent {
                it.spawnInitialPawns()
            }
        }
        task.execute(world, player, provider.buildingsManager)
    }

    override fun write(buf: PacketByteBuf?) {
        buf?.writeBlockPos(startPos)
    }

    companion object {
        const val CHANNEL = "setup_campfire"
    }
}