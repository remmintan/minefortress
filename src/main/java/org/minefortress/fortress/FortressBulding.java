package org.minefortress.fortress;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class FortressBulding {

    private final BlockPos start;
    private final BlockPos end;
    private final Set<FortressBedInfo> beds;

    public FortressBulding(BlockPos start, BlockPos end, Set<FortressBedInfo> bedPositions) {
        this.start = start.toImmutable();
        this.end = end.toImmutable();
        this.beds = Collections.unmodifiableSet(bedPositions);
    }

    public FortressBulding(NbtCompound tag) {
        if(tag.contains("start"))
            this.start = BlockPos.fromLong(tag.getLong("start"));
        else
            throw new IllegalArgumentException("Tag does not contain start");

        if (tag.contains("end"))
            this.end = BlockPos.fromLong(tag.getLong("end"));
        else
            throw new IllegalArgumentException("Tag does not contain end");

        if(tag.contains("beds")) {
            final long[] beds = tag.getLongArray("beds");
            final HashSet<FortressBedInfo> unassigned = new HashSet<>();
            for (long bed : beds) {
                unassigned.add(FortressBedInfo.fromLong(bed));
            }
            this.beds = Collections.unmodifiableSet(unassigned);
        } else {
            throw new IllegalArgumentException("Tag does not contain beds");
        }
    }

    public BlockPos getStart() {
        return start;
    }

    public BlockPos getEnd() {
        return end;
    }

    public void tick() {}

    public Optional<FortressBedInfo> getFreeBed() {
        return beds.stream().filter(b -> !b.isOccupied()).findFirst();
    }

    public void writeToNbt(NbtCompound tag) {
        tag.putLong("start", start.asLong());
        tag.putLong("end", end.asLong());

        final List<Long> beds = this.beds
                .stream()
                .map(FortressBedInfo::asLong)
                .collect(Collectors.toList());
        tag.putLongArray("beds", beds);
    }

}
