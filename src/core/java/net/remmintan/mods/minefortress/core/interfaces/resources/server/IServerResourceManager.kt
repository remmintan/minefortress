package net.remmintan.mods.minefortress.core.interfaces.resources.server

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.item.ItemStack
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager

interface IServerResourceManager : IServerManager {

    fun hasItems(stacks: List<ItemStack>): Boolean
    fun getStorage(): Storage<ItemVariant>

}
