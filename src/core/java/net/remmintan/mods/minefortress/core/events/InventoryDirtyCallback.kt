package net.remmintan.mods.minefortress.core.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

@FunctionalInterface
interface InventoryDirtyCallback {
    fun onDirty(world: ServerWorld, pos: BlockPos)

    companion object {
        val EVENT: Event<InventoryDirtyCallback> = EventFactory.createArrayBacked(
            InventoryDirtyCallback::class.java
        ) { listeners ->
            object : InventoryDirtyCallback {
                override fun onDirty(world: ServerWorld, pos: BlockPos) {
                    for (listener in listeners) {
                        listener.onDirty(world, pos)
                    }
                }
            }
        }
    }
}

