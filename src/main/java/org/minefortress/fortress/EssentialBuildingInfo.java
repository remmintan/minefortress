package org.minefortress.fortress;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class EssentialBuildingInfo {

    private final BlockPos start;
    private final BlockPos end;
    private final String requirementId;
    private final long bedsCount;

    public EssentialBuildingInfo(BlockPos start, BlockPos end, String requirementId, long bedsCount) {
        this.start = start.toImmutable();
        this.end = end.toImmutable();
        this.requirementId = requirementId;
        this.bedsCount = bedsCount;
    }

    public EssentialBuildingInfo(PacketByteBuf buf) {
        this.start = buf.readBlockPos();
        this.end = buf.readBlockPos();
        this.requirementId = buf.readString();
        this.bedsCount = buf.readLong();
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

    public void write(PacketByteBuf buffer) {
        buffer.writeBlockPos(start);
        buffer.writeBlockPos(end);
        buffer.writeString(requirementId);
        buffer.writeLong(bedsCount);
    }
}
