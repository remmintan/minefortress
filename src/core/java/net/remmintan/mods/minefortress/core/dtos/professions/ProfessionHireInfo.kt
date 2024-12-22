package net.remmintan.mods.minefortress.core.dtos.professions

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.remmintan.mods.minefortress.core.dtos.ItemInfo

data class ProfessionHireInfo(
    val professionId: String,
    val professionName: String,
    val professionItem: ItemStack,
    val professionCost: List<ItemInfo>
) {
    fun toNbt(): NbtCompound {
        val nbt = NbtCompound()
        nbt.putString("professionId", professionId)
        nbt.putString("professionName", professionName)
        nbt.put("professionItem", professionItem.writeNbt(NbtCompound()))
        nbt.put("professionCost", NbtList().apply { professionCost.forEach { add(it.toNbt()) } })
        return nbt
    }

    companion object {
        fun fromNbt(nbt: NbtCompound): ProfessionHireInfo {
            return ProfessionHireInfo(
                nbt.getString("professionId"),
                nbt.getString("professionName"),
                ItemStack.fromNbt(nbt.getCompound("professionItem")),
                nbt.getList("professionCost", 10).map { ItemInfo.fromNbt(it as NbtCompound) }
            )
        }
    }
}
