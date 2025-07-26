package org.minefortress.fortress.resources.client

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.resources.IClientResourceManager

@Suppress("UnstableApiUsage")
class ClientResourceManager : IClientResourceManager {

    private var containerPositions = emptyList<BlockPos>()

    override fun getStorage(): Storage<ItemVariant> {
        val world = MinecraftClient.getInstance().world
        val storages = containerPositions.map { ItemStorage.SIDED.find(world, it, null) }
        return CombinedStorage(storages)
    }

    override fun sync(containerPositions: List<BlockPos>) {
        this.containerPositions = containerPositions
    }
}
