package org.minefortress.fortress.buildings;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class EssentialBuildingInfo {

    public static final String DEFAULT_BLUEPRINT_ID = "default-file-36d9a49d-4d47-45c8-9201-23d71e156da1";

    private final UUID id;
    private final BlockPos start;
    private final BlockPos end;
    private final String requirementId;
    private final long bedsCount;
    @NotNull
    private final String blueprintId;

    public EssentialBuildingInfo(UUID id, BlockPos start, BlockPos end, String requirementId, long bedsCount, @Nullable String blueprintId) {
        this.id = id;
        this.start = start.toImmutable();
        this.end = end.toImmutable();
        this.requirementId = requirementId;
        this.bedsCount = bedsCount;
        this.blueprintId = Optional.ofNullable(blueprintId).orElse(DEFAULT_BLUEPRINT_ID);
    }

    public EssentialBuildingInfo(PacketByteBuf buf) {
        this.id = buf.readUuid();
        this.start = buf.readBlockPos();
        this.end = buf.readBlockPos();
        this.requirementId = buf.readString();
        this.bedsCount = buf.readLong();
        this.blueprintId = buf.readString();
    }

    public BlockPos getStart() {
        return start;
    }

    public BlockPos getEnd() {
        return end;
    }

    public String getRequirementId() {
        return requirementId;
    }

    public long getBedsCount() {
        return bedsCount;
    }

    public UUID getId() {
        return id;
    }

    @NotNull
    public Optional<String> getBlueprintId() {
        if(blueprintId.equals(DEFAULT_BLUEPRINT_ID)) return Optional.empty();
        return Optional.of(blueprintId);
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeUuid(id);
        buffer.writeBlockPos(start);
        buffer.writeBlockPos(end);
        buffer.writeString(requirementId);
        buffer.writeLong(bedsCount);
        buffer.writeString(blueprintId);
    }
}
