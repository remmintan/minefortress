package org.minefortress.fortress.resources.server

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.resources.server.IServerContainersRegistry
import net.remmintan.mods.minefortress.core.interfaces.resources.server.IServerFoodManager
import net.remmintan.mods.minefortress.core.interfaces.resources.server.IServerResourceManager
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager
import net.remmintan.mods.minefortress.core.utils.LogCompanion

private const val CONTAINER_POSITIONS_NBT_KEY = "containerPositions"

private val FARMER_SEEDS = listOf(
    Items.WHEAT_SEEDS,
    Items.BEETROOT_SEEDS,
    Items.CARROT,
    Items.POTATO
)

@Suppress("UnstableApiUsage")
class ServerResourceManager(private val server: MinecraftServer) :
    IServerResourceManager,
    IServerContainersRegistry,
    IServerFoodManager,
    IWritableManager {

    private val world: ServerWorld by lazy { server.overworld }
    private val foodItems: Set<Item> by lazy {
        Registries.ITEM
            .filter {
                it.foodComponent?.statusEffects?.none { it.first.effectType.category == StatusEffectCategory.HARMFUL } == true
            }
            .toSet()
    }

    private val containerPositions = mutableSetOf<BlockPos>()


    override fun register(pos: BlockPos) {
        containerPositions.add(pos.toImmutable())
    }

    override fun unregister(pos: BlockPos) {
        containerPositions.remove(pos.toImmutable())
    }

    override fun hasFood(): Boolean {
        return findFood() != null
    }

    override fun getFood(): ItemStack? {
        val item = findFood() ?: return null
        Transaction.openOuter().use { tr ->
            val variant = ItemVariant.of(item)
            val extractedAmount = getStorage().extract(variant, 1, tr)
            if (extractedAmount == 1L) {
                tr.commit()
                return ItemStack(item)
            }
        }

        return null
    }

    override fun getFarmerSeeds(): Item? {
        val storage = getStorage()
        Transaction.openOuter().use { tr ->
            FARMER_SEEDS.forEach {
                val extracted = storage.extract(ItemVariant.of(it), 1, tr)
                if (extracted == 1L) {
                    tr.commit()
                    return it
                }
            }
        }
        return null
    }

    private fun findFood(): Item? {
        for (view in getStorage()) {
            if (view.isResourceBlank) continue
            val item = view.resource.item
            if (foodItems.contains(item))
                return item
        }
        return null
    }


    override fun write(tag: NbtCompound) {
        tag.putLongArray(CONTAINER_POSITIONS_NBT_KEY, containerPositions.map { it.asLong() })
    }

    override fun read(tag: NbtCompound) {
        if (tag.contains(CONTAINER_POSITIONS_NBT_KEY)) {
            val positions = tag.getLongArray(CONTAINER_POSITIONS_NBT_KEY).map { BlockPos.fromLong(it) }
            containerPositions.clear()
            containerPositions.addAll(positions)
        }
    }

    override fun getStorage(): Storage<ItemVariant> {
        val storages = mutableListOf<Storage<ItemVariant>>()
        for (pos in containerPositions.toList()) {
            val storage = ItemStorage.SIDED.find(world, pos, null)
            if (storage == null || !storage.supportsInsertion() || !storage.supportsExtraction()) {
                log.error("Storage at position $pos is no longer valid! Unregistering")
                unregister(pos)
                continue
            }
            storages.add(storage)
        }

        return CombinedStorage(storages)
    }

    companion object : LogCompanion(ServerResourceManager::class)
}