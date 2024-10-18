package net.remmintan.mods.minefortress.building

import baritone.api.minefortress.IFortressAwareBlockEntity
import baritone.api.minefortress.IMinefortressEntity
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn

private const val TIMEOUT: Long = 30

class FortressScaffoldBlockEntity(pos: BlockPos?, state: BlockState?) :
    BlockEntity(FortressBlocks.SCAFFOLD_ENT_TYPE, pos, state), IFortressAwareBlockEntity {

    private var placedTimestamp: Long = 0L
    private var placer: IWorkerPawn? = null

    fun onPlace(world: World) {
        placedTimestamp = world.time
    }

    fun tick(world: World, pos: BlockPos?, state: BlockState?) {
        if (world.isClient) return
        if (state?.isOf(FortressBlocks.SCAFFOLD_OAK_PLANKS) == true && world.time - placedTimestamp > TIMEOUT) {
            if (placer?.taskControl?.hasTask() != true) {
                world.removeBlock(pos, false)
                world.emitGameEvent(placer as? Entity, GameEvent.BLOCK_DESTROY, pos)
            }
        }
    }

    override fun writeNbt(nbt: NbtCompound?) {
        super.writeNbt(nbt)
        nbt?.putLong("placedTimestamp", placedTimestamp)
    }

    override fun readNbt(nbt: NbtCompound?) {
        super.readNbt(nbt)
        placedTimestamp = nbt?.getLong("placedTimestamp") ?: 0
    }

    override fun setPlacer(minefortressEntity: IMinefortressEntity?) {
        if (minefortressEntity is IWorkerPawn) this.placer = minefortressEntity
    }
}