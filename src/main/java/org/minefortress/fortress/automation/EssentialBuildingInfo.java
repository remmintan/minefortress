package org.minefortress.fortress.automation;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EssentialBuildingInfo {

    public static final String DEFAULT_FILE = "default-file-36d9a49d-4d47-45c8-9201-23d71e156da1";

    private final BlockPos start;
    private final BlockPos end;
    private final String requirementId;
    private final long bedsCount;
    @NotNull
    private final String file;

    public EssentialBuildingInfo(BlockPos start, BlockPos end, String requirementId, long bedsCount, @Nullable String file) {
        this.start = start.toImmutable();
        this.end = end.toImmutable();
        this.requirementId = requirementId;
        this.bedsCount = bedsCount;
        this.file = Optional.ofNullable(file).orElse(DEFAULT_FILE);
    }

    public EssentialBuildingInfo(PacketByteBuf buf) {
        this.start = buf.readBlockPos();
        this.end = buf.readBlockPos();
        this.requirementId = buf.readString();
        this.bedsCount = buf.readLong();
        this.file = buf.readString();
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

    @NotNull
    public String getFile() {
        return file;
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeBlockPos(start);
        buffer.writeBlockPos(end);
        buffer.writeString(requirementId);
        buffer.writeLong(bedsCount);
        buffer.writeString(file);
    }
}
