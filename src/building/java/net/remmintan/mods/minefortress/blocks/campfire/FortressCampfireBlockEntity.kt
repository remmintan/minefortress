package net.remmintan.mods.minefortress.blocks.campfire

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.remmintan.mods.minefortress.blocks.FortressBlocks
import net.remmintan.mods.minefortress.core.interfaces.entities.player.IFortressPlayerEntity
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressHolder
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider
import net.remmintan.mods.minefortress.core.services.FortressServiceLocator

class FortressCampfireBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(FortressBlocks.CAMPFIRE_ENT_TYPE, pos, state), IFortressHolder {

    private var mpTag: NbtCompound? = null
    private var fmTag: NbtCompound? = null

    private val fortressManager: IServerFortressManager by lazy {
        val manager = FortressServiceLocator.get(IServerFortressManager::class.java, super.pos, world as? ServerWorld)
        fmTag?.let { manager.read(it) }
        manager
    }
    private val managersProvider: IServerManagersProvider by lazy {
        val provider = FortressServiceLocator.get(IServerManagersProvider::class.java, super.pos, world as? ServerWorld)
        mpTag?.let { provider.read(it) }
        provider
    }


    fun tick(world: World, pos: BlockPos?, state: BlockState?) {
        if (world.isClient) return

        managersProvider.tick(world.server, world as ServerWorld)
        fortressManager.tick(getFortressOwner())
    }

    override fun writeNbt(nbt: NbtCompound?) {
        super.writeNbt(nbt)

        val mpTag = NbtCompound()
        managersProvider.write(mpTag)
        nbt?.put("managersProvider", mpTag)

        val fmTag = NbtCompound()
        fortressManager.write(fmTag)
        nbt?.put("fortressManager", fmTag)
    }

    override fun readNbt(nbt: NbtCompound?) {
        super.readNbt(nbt)
        if (nbt?.contains("managersProvider") == true)
            mpTag = nbt.getCompound("managersProvider")

        if (nbt?.contains("fortressManager") == true)
            fmTag = nbt.getCompound("fortressManager")

    }

    override fun getServerFortressManager() = fortressManager
    override fun getServerManagersProvider() = managersProvider
    override fun getFortressOwner(): ServerPlayerEntity? {
        return world?.server?.playerManager?.playerList?.find {
            (it as IFortressPlayerEntity).get_FortressPos().map { p -> p.equals(pos) }.orElse(false)
        }
    }
}