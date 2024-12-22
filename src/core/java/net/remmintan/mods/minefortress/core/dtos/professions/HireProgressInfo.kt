package net.remmintan.mods.minefortress.core.dtos.professions

import net.minecraft.nbt.NbtCompound

data class HireProgressInfo(
    val professionId: String,
    val queueLength: Int,
    val currentCount: Int,
    val maxCount: Int,
    val progress: Int,
    val canHireMore: Boolean
) {
    fun toNbt(): NbtCompound {
        val nbt = NbtCompound()
        nbt.putString("professionId", professionId)
        nbt.putInt("queueLength", queueLength)
        nbt.putInt("currentCount", currentCount)
        nbt.putInt("maxCount", maxCount)
        nbt.putInt("progress", progress)
        nbt.putBoolean("canHireMore", canHireMore)
        return nbt
    }

    companion object {
        fun fromNbt(nbt: NbtCompound): HireProgressInfo {
            return HireProgressInfo(
                nbt.getString("professionId"),
                nbt.getInt("queueLength"),
                nbt.getInt("currentCount"),
                nbt.getInt("maxCount"),
                nbt.getInt("progress"),
                nbt.getBoolean("canHireMore")
            )
        }

        fun getEmpty(professionId: String): HireProgressInfo = HireProgressInfo(professionId, 0, 0, 0, 0, false)
    }
}
