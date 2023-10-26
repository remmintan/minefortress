package net.remmintan.mods.minefortress.core.interfaces.entities;

import net.minecraft.nbt.NbtCompound;

public interface IPawnNameGenerator {
    String generateRandomName();

    void write(NbtCompound compound);
}
