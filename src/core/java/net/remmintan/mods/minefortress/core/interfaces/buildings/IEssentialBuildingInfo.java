package net.remmintan.mods.minefortress.core.interfaces.buildings;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintRequirement;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;

import java.util.Optional;
import java.util.UUID;

public interface IEssentialBuildingInfo {
    BlueprintRequirement getRequirement();
    BlockPos getStart();

    BlockPos getEnd();

    long getBedsCount();

    UUID getId();

    String getBlueprintId();

    int getHealth();

    void write(PacketByteBuf buffer);

    default boolean satisfiesRequirement(ProfessionType type, int level) {
        final var requirement = getRequirement();
        return Optional
                .ofNullable(requirement.getType())
                .map(t -> t.equals(type))
                .orElse(false)
                && requirement.getLevel() >= level;
    }
}
