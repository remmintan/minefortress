package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.nbt.NbtCompound;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface IServerStructureBlockDataManager extends IBlockDataProvider {
    Optional<Integer> getFloorLevel(String filename);

    Optional<NbtCompound> getStructureNbt(String fileName);

    boolean update(String fileName, NbtCompound tag, int newFloorLevel, BlueprintGroup group);

    List<FortressS2CPacket> getInitPackets();

    void remove(String fileName);

    void writeBlockDataManager();

    @NotNull String getBlueprintsFolder();

    void readBlockDataManager(@Nullable NbtCompound tag);
}
