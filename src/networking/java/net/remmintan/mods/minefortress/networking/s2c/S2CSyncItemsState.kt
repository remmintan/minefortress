package net.remmintan.mods.minefortress.networking.s2c

import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket
import net.remmintan.mods.minefortress.core.utils.ClientModUtils

class S2CSyncItemsState(private val stacks: List<ItemStack>) : FortressS2CPacket {

    constructor(buf: PacketByteBuf) : this(buf.readCollection({ mutableListOf<ItemStack>() }) { it.readItemStack() })

    override fun write(buf: PacketByteBuf) {
        buf.writeCollection(stacks) { b, st -> b.writeItemStack(st) }
    }

    override fun handle(client: MinecraftClient?) {
        val resourceManager = ClientModUtils.getFortressManager().resourceManager
        resourceManager.syncRequestedItems(stacks)
    }

    companion object {
        const val CHANNEL = "fortress_sync_items_state"
    }
}