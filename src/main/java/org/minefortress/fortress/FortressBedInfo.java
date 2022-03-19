package org.minefortress.fortress;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class FortressBedInfo {

    private final BlockPos pos;
    private boolean occupied;

    public FortressBedInfo(BlockPos pos) {
        this.pos = pos;
        this.occupied = false;
    }

    public FortressBedInfo(BlockPos pos, boolean occupied) {
        this.pos = pos;
        this.occupied = occupied;
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public long asLong() {
        return pos.asLong() << 1 | (occupied ? 1 : 0);
    }

    public static FortressBedInfo fromLong(long l) {
        return new FortressBedInfo(BlockPos.fromLong(l >> 1), (l & 1) == 1);
    }
}
