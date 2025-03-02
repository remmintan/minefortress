package net.remmintan.mods.minefortress.blocks.campfire

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.remmintan.mods.minefortress.blocks.FortressBlocks
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressHolder
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider
import net.remmintan.mods.minefortress.core.services.FortressServiceLocator
import java.util.*

class FortressCampfireBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(FortressBlocks.CAMPFIRE_ENT_TYPE, pos, state), IFortressHolder {

    var placerId: UUID? = null
    private var fortressManager: IServerFortressManager =
        FortressServiceLocator.get(IServerFortressManager::class.java, pos)
    private val managersProvider: IServerManagersProvider =
        FortressServiceLocator.get(IServerManagersProvider::class.java, pos)

    fun tick(world: World, pos: BlockPos?, state: BlockState?) {
        if (world.isClient) return

        managersProvider.tick(world.server, world as ServerWorld)
        fortressManager.tick(getFortressOwner())
    }

    override fun writeNbt(nbt: NbtCompound?) {
        super.writeNbt(nbt)
        if (placerId != null) nbt?.putUuid("placerId", placerId)

        val mpTag = NbtCompound()
        managersProvider.write(mpTag)
        nbt?.put("managersProvider", mpTag)

        val fmTag = NbtCompound()
        fortressManager.write(fmTag)
        nbt?.put("fortressManager", fmTag)
    }

    override fun readNbt(nbt: NbtCompound?) {
        super.readNbt(nbt)
        if (nbt?.contains("placerId") == true) {
            placerId = nbt.getUuid("placerId")
        }
        if (nbt?.contains("managersProvider") == true) {
            val mpTag = nbt.getCompound("managersProvider")
            managersProvider.read(mpTag)
        }

        if (nbt?.contains("fortressManager") == true) {
            val fmTag = nbt.getCompound("fortressManager")
            fortressManager.read(fmTag)
        }
    }

    override fun getServerFortressManager() = fortressManager
    override fun getServerManagersProvider() = managersProvider
    override fun getFortressOwner(): ServerPlayerEntity? {
        return world?.server?.playerManager?.getPlayer(placerId)
    }
}