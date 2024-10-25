package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.nbt.NbtCompound;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface IServerStructureBlockDataManager extends IBlockDataProvider {
    Optional<Integer> getFloorLevel(String blueprintId);

    Optional<NbtCompound> getStructureNbt(String blueprintId);

    boolean update(String blueprintId, NbtCompound tag, int newFloorLevel, int capacity, BlueprintGroup group);

    List<FortressS2CPacket> getInitPackets();

    void remove(String blueprintId);

    void writeBlockDataManager();

    @NotNull String getBlueprintsFolder();

    void readBlockDataManager();
}
