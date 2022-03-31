package org.minefortress.fortress;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class EssentialBuildingInfo {

    private final BlockPos start;
    private final BlockPos end;
    private final String requirementId;

    public EssentialBuildingInfo(BlockPos start, BlockPos end, String requirementId) {
        this.start = start.toImmutable();
        this.end = end.toImmutable();
        this.requirementId = requirementId;
    }

    public EssentialBuildingInfo(PacketByteBuf buf) {
        this.start = buf.readBlockPos();
        this.end = buf.readBlockPos();
        this.requirementId = buf.readString();
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

    public void write(PacketByteBuf buffer) {
        buffer.writeBlockPos(start);
        buffer.writeBlockPos(end);
        buffer.writeString(requirementId);
    }
}
