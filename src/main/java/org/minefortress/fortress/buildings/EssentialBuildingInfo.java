package org.minefortress.fortress.buildings;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintRequirement;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IEssentialBuildingInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class EssentialBuildingInfo implements IEssentialBuildingInfo {

    public static final String DEFAULT_BLUEPRINT_ID = "default-file-36d9a49d-4d47-45c8-9201-23d71e156da1";

    private final UUID id;
    private final BlockPos start;
    private final BlockPos end;
    private final long bedsCount;
    @NotNull
    private final String blueprintId;
    private final int health;

    public EssentialBuildingInfo(UUID id,
                                 BlockPos start,
                                 BlockPos end,
                                 long bedsCount,
                                 @Nullable String blueprintId,
                                 int health) {
        this.id = id;
        this.start = start.toImmutable();
        this.end = end.toImmutable();
        this.bedsCount = bedsCount;
        this.blueprintId = Optional.ofNullable(blueprintId).orElse(DEFAULT_BLUEPRINT_ID);
        this.health = health;
    }

    public EssentialBuildingInfo(PacketByteBuf buf) {
        this.id = buf.readUuid();
        this.start = buf.readBlockPos();
        this.end = buf.readBlockPos();
        this.bedsCount = buf.readLong();
        this.blueprintId = buf.readString();
        this.health = buf.readInt();
    }

    @Override
    public BlockPos getStart() {
        return start;
    }

    @Override
    public BlockPos getEnd() {
        return end;
    }

    @Override
    public long getBedsCount() {
        return bedsCount;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    @NotNull
    public Optional<String> getBlueprintId() {
        if(blueprintId.equals(DEFAULT_BLUEPRINT_ID)) return Optional.empty();
        return Optional.of(blueprintId);
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeUuid(id);
        buffer.writeBlockPos(start);
        buffer.writeBlockPos(end);
        buffer.writeLong(bedsCount);
        buffer.writeString(blueprintId);
        buffer.writeInt(health);
    }

    @Override
    public Optional<BlueprintRequirement> getRequirement() {
        return getBlueprintId().map(BlueprintRequirement::new);
    }
}
