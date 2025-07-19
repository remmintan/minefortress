package org.minefortress.fortress.resources.server

import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.dtos.ItemInfo
import net.remmintan.mods.minefortress.core.events.InventoryDirtyCallback
import net.remmintan.mods.minefortress.core.interfaces.resources.server.IEatableItemsManager
import net.remmintan.mods.minefortress.core.interfaces.resources.server.IServerContainersRegistry
import net.remmintan.mods.minefortress.core.interfaces.resources.server.IServerResourceManager
import net.remmintan.mods.minefortress.core.interfaces.server.ISyncableServerManager
import net.remmintan.mods.minefortress.core.interfaces.server.ITickableManager
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager
import net.remmintan.mods.minefortress.core.utils.isCreativeFortress
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper
import net.remmintan.mods.minefortress.networking.s2c.ClientboundSyncItemsPacket

private const val CONTAINER_POSITIONS_NBT_KEY = "containerPositions"

class ServerResourceManager(private val server: MinecraftServer) :
    IServerResourceManager,
    IServerContainersRegistry,
    IEatableItemsManager,
    ITickableManager,
    ISyncableServerManager,
    IWritableManager,
    InventoryDirtyCallback {

    private val synchronizer = Synchronizer()
    private val containerPositions = mutableSetOf<BlockPos>()

    private var itemCounts = mapOf<Item, Int>()
    private var isDirty = true

    init {
        InventoryDirtyCallback.EVENT.register(this)
    }

    override fun onDirty(world: ServerWorld, pos: BlockPos) {
        if (containerPositions.contains(pos))
            this.sync()
    }

    override fun register(pos: BlockPos) {
        if (containerPositions.add(pos.toImmutable())) {
            isDirty = true
        }
    }

    override fun unregister(pos: BlockPos) {
        if (containerPositions.remove(pos.toImmutable())) {
            isDirty = true
        }
    }

    // --- IServerResourceManager Core Logic ---

    override fun findContainerToPut(item: Item): BlockPos? {
        // 1. Prioritize containers that already have a non-full stack of this item.
        for (pos in containerPositions) {
            val inventory = getInventory(pos) ?: continue
            for (i in 0 until inventory.size()) {
                val stack = inventory.getStack(i)
                if (stack.item == item && stack.count < stack.maxCount) {
                    return pos // Found a non-full stack of the same item
                }
            }
        }

        // 2. Find any container with an empty slot.
        for (pos in containerPositions) {
            val inventory = getInventory(pos) ?: continue
            if (hasEmptySlot(inventory)) {
                return pos
            }
        }

        return null // No space available
    }

    override fun findContainerToGet(item: Item): BlockPos? {
        for (pos in containerPositions) {
            val inventory = getInventory(pos) ?: continue
            if (inventory.count(item) > 0) {
                return pos
            }
        }
        return null
    }

    override fun hasItems(infos: List<ItemInfo>): Boolean {
        if (server.isCreativeFortress()) return true

        val currentCounts = itemCounts.toMutableMap()

        for (requiredInfo in infos) {
            val requiredItem = requiredInfo.item
            var amountNeeded = requiredInfo.amount

            val availableAmount = currentCounts.getOrDefault(requiredItem, 0)
            if (availableAmount >= amountNeeded) {
                currentCounts[requiredItem] = availableAmount - amountNeeded
            } else {
                return false // Not enough of the required item
            }
        }
        return true
    }

    // --- IEatableItemsManager Implementation ---

    override fun findContainerWithFood(): BlockPos? {
        for (pos in containerPositions) {
            val inventory = getInventory(pos) ?: continue
            for (i in 0 until inventory.size()) {
                if (isEatable(inventory.getStack(i).item)) {
                    return pos
                }
            }
        }
        return null
    }

    // --- Tick, Sync, and Persistence ---

    override fun tick(server: MinecraftServer, world: ServerWorld, player: ServerPlayerEntity?) {
        if (isDirty) {
            recalculateCacheAndScheduleSync()
            isDirty = false
        }
        synchronizer.sync(player)
    }

    override fun write(tag: NbtCompound) {
        tag.putLongArray(CONTAINER_POSITIONS_NBT_KEY, containerPositions.map { it.asLong() })
    }

    override fun read(tag: NbtCompound) {
        if (tag.contains(CONTAINER_POSITIONS_NBT_KEY)) {
            val positions = tag.getLongArray(CONTAINER_POSITIONS_NBT_KEY).map { BlockPos.fromLong(it) }
            containerPositions.clear()
            containerPositions.addAll(positions)
            isDirty = true
        }
    }

    override fun sync() {
        isDirty = true
    }

    // --- Private Helper Methods ---

    private fun getInventory(pos: BlockPos): Inventory? {
        return server.overworld.getBlockEntity(pos) as? Inventory
    }

    /**
     * Helper to check for an empty slot since Inventory interface does not have it directly.
     */
    private fun hasEmptySlot(inventory: Inventory): Boolean {
        for (i in 0 until inventory.size()) {
            if (inventory.getStack(i).isEmpty) {
                return true
            }
        }
        return false
    }

    private fun recalculateCacheAndScheduleSync() {
        val newCounts = mutableMapOf<Item, Int>()
        for (pos in containerPositions) {
            val inventory = getInventory(pos) ?: continue
            for (i in 0 until inventory.size()) {
                val stack = inventory.getStack(i)
                if (!stack.isEmpty) {
                    newCounts.merge(stack.item, stack.count, Int::plus)
                }
            }
        }
        itemCounts = newCounts.toMap()
        synchronizer.syncAll(itemCounts)
    }

    private class Synchronizer {
        private var itemsToSync: List<ItemInfo>? = null
        private var fullSync = false

        fun sync(player: ServerPlayerEntity?) {
            if (player == null || itemsToSync == null) return

            val packet = ClientboundSyncItemsPacket(itemsToSync, fullSync)
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_RESOURCES_SYNC, packet)

            itemsToSync = null
            fullSync = false
        }

        fun syncAll(counts: Map<Item, Int>) {
            itemsToSync = counts.map { ItemInfo(it.key, it.value) }
            fullSync = true
        }
    }

    companion object {
        private fun isEatable(item: Item): Boolean {
            if (!item.isFood) return false

            val foodComponent = item.foodComponent ?: return false
            return foodComponent.statusEffects.none { it.first.effectType.category == StatusEffectCategory.HARMFUL }
        }
    }
}