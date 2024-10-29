package net.remmintan.mods.minefortress.core.interfaces.buildings;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintRequirement;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public interface IEssentialBuildingInfo {
    Optional<BlueprintRequirement> getRequirement();
    BlockPos getStart();

    BlockPos getEnd();

    long getBedsCount();

    UUID getId();

    @NotNull Optional<String> getBlueprintId();

    int getHealth();

    void write(PacketByteBuf buffer);

    default boolean satisfiesRequirement(ProfessionType type, int level) {
        return getRequirement()
                .map(it -> Optional
                        .ofNullable(it.getType())
                        .map(t -> t.equals(type))
                        .orElse(false)
                        && it.getLevel() >= level)
                .orElse(false);
    }
}
