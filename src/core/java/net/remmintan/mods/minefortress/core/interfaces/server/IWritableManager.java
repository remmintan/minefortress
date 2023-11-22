package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.nbt.NbtCompound;

public interface IWritableManager {
    void writeToNbt(NbtCompound tag);
    void readFromNbt(NbtCompound tag);
}
