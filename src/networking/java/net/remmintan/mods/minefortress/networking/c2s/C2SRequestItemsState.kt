package net.remmintan.mods.minefortress.networking.c2s

import net.minecraft.item.Item
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket
import net.remmintan.mods.minefortress.core.utils.ServerModUtils

class C2SRequestItemsState(private val items: Set<Item>) : FortressC2SPacket {

    constructor(buf: PacketByteBuf) : this(buf.readCollection({ _ -> mutableSetOf<Item>() }) { b -> Item.byRawId(b.readVarInt()) })

    override fun handle(server: MinecraftServer, player: ServerPlayerEntity) {
        ServerModUtils.getManagersProvider(player).orElseThrow().resourceHelper.syncRequestedItems(items, player)
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeCollection(items) { b, i -> b.writeVarInt(Item.getRawId(i)) }
    }

    companion object {
        const val CHANNEL = "fortress_request_items_state"
    }

}