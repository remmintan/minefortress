package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.nbt.NbtCompound;

public interface IWritableManager {
    void write(NbtCompound tag);
    void read(NbtCompound tag);
}
