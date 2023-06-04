package org.minefortress.fortress.automation;

import net.minecraft.block.BedBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.fortress.IAutomationArea;
import org.minefortress.fortress.automation.iterators.FarmBuildingIterator;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FortressBuilding implements IAutomationArea {

    private final UUID id;
    private final BlockPos start;
    private final BlockPos end;
    private final String requirementId;
    @Nullable
    private final String blueprintId;
    private LocalDateTime lastUpdated;
    private Iterator<AutomationBlockInfo> currentIterator;

    public FortressBuilding(UUID id, BlockPos start, BlockPos end, String requirementId, @NotNull String blueprintId) {
        this.id = id;
        this.start = start.toImmutable();
        this.end = end.toImmutable();
        this.requirementId = requirementId;
        this.lastUpdated = LocalDateTime.MIN;
        this.blueprintId = blueprintId;
    }

    public FortressBuilding(NbtCompound tag) {
        if(tag.contains("id")) {
            this.id = tag.getUuid("id");
        } else {
            this.id = UUID.randomUUID();
        }

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

        if(tag.contains("lastUpdated"))
            this.lastUpdated = LocalDateTime.parse(tag.getString("lastUpdated"));
        else
            this.lastUpdated = LocalDateTime.MIN;

        if(tag.contains("blueprintId"))
            this.blueprintId = tag.getString("blueprintId");
        else if(tag.contains("file")) // support old format
            this.blueprintId = tag.getString("file");
        else
            this.blueprintId = null;
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
                .filter(pos -> world.getBlockState(pos).get(BedBlock.PART) == BedPart.HEAD)
                .map(BlockPos::toImmutable);
    }

    public long getBedsCount(World world) {
        return streamBeds(world).count();
    }

    public void writeToNbt(NbtCompound tag) {
        tag.putUuid("id", id);
        tag.putLong("start", start.asLong());
        tag.putLong("end", end.asLong());
        tag.putString("requirementId", requirementId);
        tag.putString("lastUpdated", lastUpdated.toString());
        if(blueprintId != null) {
            tag.putString("blueprintId", blueprintId);
        }
    }

    @Override
    public Iterator<AutomationBlockInfo> iterator(World world) {
        if (currentIterator == null || !currentIterator.hasNext()) {
            if (requirementId.startsWith("farm")) {
                this.currentIterator = new FarmBuildingIterator(start, end, world);
            }
        }
        if(this.currentIterator == null) {
            throw new IllegalStateException("Iterator is not set properly");
        }

        return currentIterator;
    }

    public boolean satisfiesRequirement(String requirementId) {
        return this.requirementId != null && this.requirementId.equals(requirementId);
    }

    public UUID getId() {
        return id;
    }

    @Override
    public void update() {
        this.lastUpdated = LocalDateTime.now();
    }

    @Override
    public LocalDateTime getUpdated() {
        return lastUpdated;
    }

    public EssentialBuildingInfo toEssentialInfo(World world) {
        return new EssentialBuildingInfo(id, start, end, requirementId, getBedsCount(world), blueprintId);
    }
}
