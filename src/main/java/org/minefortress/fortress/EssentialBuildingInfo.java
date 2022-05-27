package org.minefortress.fortress;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class EssentialBuildingInfo {

    private final BlockPos start;
    private final BlockPos end;
    private final String requirementId;
    private final int bedsCount;

    public EssentialBuildingInfo(BlockPos start, BlockPos end, String requirementId, int bedsCount) {
        this.start = start.toImmutable();
        this.end = end.toImmutable();
        this.requirementId = requirementId;
        this.bedsCount = bedsCount;
    }

    public EssentialBuildingInfo(PacketByteBuf buf) {
        this.start = buf.readBlockPos();
        this.end = buf.readBlockPos();
        this.requirementId = buf.readString();
        this.bedsCount = buf.readInt();
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

    public int getBedsCount() {
        return bedsCount;
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeBlockPos(start);
        buffer.writeBlockPos(end);
        buffer.writeString(requirementId);
        buffer.writeInt(bedsCount);
    }
}
