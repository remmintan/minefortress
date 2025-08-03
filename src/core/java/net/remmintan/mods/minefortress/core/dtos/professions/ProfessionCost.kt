package net.remmintan.mods.minefortress.core.dtos.professions

import net.minecraft.item.Item
import net.minecraft.network.PacketByteBuf

data class ProfessionCost(val item: Item, val requiredAmount: Int, val totalAmount: Long) {

    val enoughItems: Boolean = totalAmount >= requiredAmount

    fun writeToPacketByteBuf(buf: PacketByteBuf) {
        buf.writeInt(Item.getRawId(item))
        buf.writeInt(requiredAmount)
        buf.writeLong(totalAmount)
    }

    companion object {

        fun readFromPacketByteBuf(buf: PacketByteBuf): ProfessionCost {
            val item = Item.byRawId(buf.readInt())
            val requiredAmount = buf.readInt()
            val totalAmount = buf.readLong()

            return ProfessionCost(item, requiredAmount, totalAmount)
        }

    }

}
