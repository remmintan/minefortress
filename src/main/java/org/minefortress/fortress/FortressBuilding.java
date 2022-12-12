package org.minefortress.fortress;

import net.minecraft.block.BedBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FortressBuilding {

    private final BlockPos start;
    private final BlockPos end;
    private final String requirementId;

    public FortressBuilding(BlockPos start, BlockPos end, String requirementId) {
        this.start = start.toImmutable();
        this.end = end.toImmutable();
        this.requirementId = requirementId;
    }

    public FortressBuilding(NbtCompound tag) {
        if(tag.contains("start"))
            this.start = BlockPos.fromLong(tag.getLong("start"));
        else
            throw new IllegalArgumentException("Tag does not contain start");

        if (tag.contains("end"))
            this.end = BlockPos.fromLong(tag.getLong("end"));
        else
            throw new IllegalArgumentException("Tag does not contain end");

        if(tag.contains("requirementId"))
            this.requirementId = tag.getString("requirementId");
        else
            this.requirementId = "<old>";
    }

    public BlockPos getStart() {
        return start;
    }

    public BlockPos getEnd() {
        return end;
    }

    public Optional<BlockPos> getFreeBed(World world) {
        return streamBeds(world)
                .filter(pos -> !world.getBlockState(pos).get(BedBlock.OCCUPIED))
                .findFirst();
    }

    @NotNull
    private Stream<BlockPos> streamBeds(World world) {
        return StreamSupport.stream(BlockPos.iterate(start, end).spliterator(), false)
                .filter(pos -> world.getBlockState(pos).isIn(BlockTags.BEDS))
                .filter(pos -> world.getBlockState(pos).get(BedBlock.PART) == BedPart.FOOT)
                .map(BlockPos::toImmutable);
    }

    public long getBedsCount(World world) {
        return streamBeds(world).count();
    }

    public void writeToNbt(NbtCompound tag) {
        tag.putLong("start", start.asLong());
        tag.putLong("end", end.asLong());

        tag.putString("requirementId", requirementId);
    }

    public String getRequirementId() {
        return requirementId;
    }
}
