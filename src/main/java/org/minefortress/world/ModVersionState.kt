package org.minefortress.world

import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.world.PersistentState

private const val CURRENT_MINEFORTRESS_VERSION = 2

class ModVersionState : PersistentState() {

    private var version: Int = -1

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        nbt.putInt("version", version)
        return nbt
    }

    fun readNbt(nbt: NbtCompound) {
        version = nbt.getInt("version")
    }

    fun isOutdated(): Boolean {
        return version < CURRENT_MINEFORTRESS_VERSION
    }

    fun setToCurrentVersion() {
        version = CURRENT_MINEFORTRESS_VERSION
        markDirty()
    }

    companion object {
        private const val ID = "minefortress_data_version"
        private val TYPE = Type(
            { ModVersionState() },
            { nbt: NbtCompound -> ModVersionState().apply { readNbt(nbt) } },
            null
        )

        fun get(server: MinecraftServer): ModVersionState? {
            return server.overworld.persistentStateManager.get(TYPE, ID)
        }
    }
}