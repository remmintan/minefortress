package net.remmintan.mods.minefortress.core.interfaces.buildings;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public interface IEssentialBuildingInfo {
    BlockPos getStart();

    BlockPos getEnd();

    String getRequirementId();

    long getBedsCount();

    UUID getId();

    @NotNull Optional<String> getBlueprintId();

    int getHealth();

    void write(PacketByteBuf buffer);
}
