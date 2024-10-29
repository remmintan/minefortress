package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

public interface IServerStructureBlockDataManager extends IBlockDataProvider {

    Optional<NbtCompound> getStructureNbt(String blueprintId);

    void addOrUpdate(String blueprintId, NbtCompound tag);

    void remove(String blueprintId);

}
