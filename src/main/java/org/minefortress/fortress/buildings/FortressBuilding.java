package org.minefortress.fortress.buildings;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IEssentialBuildingInfo;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.fortress.automation.iterators.FarmBuildingIterator;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FortressBuilding implements IFortressBuilding {

    private static final int MAX_BLOCKS_PER_UPDATE = 10;

    private final UUID id;
    private final BlockPos start;
    private final BlockPos end;
    @Nullable
    private final String blueprintId;
    private final int floorLevel;
    @Nullable
    private FortressBuildingBlockData buildingBlockData;
    private LocalDateTime lastUpdated;
    private Iterator<IAutomationBlockInfo> currentIterator;

    private final Set<HostileEntity> attackers = new HashSet<>();

    public FortressBuilding(UUID id,
                            BlockPos start,
                            BlockPos end,
                            @NotNull String blueprintId,
                            int floorLevel,
                            Map<BlockPos, BlockState> buildingBlockData
    ) {
        this.id = id;
        this.start = start.toImmutable();
        this.end = end.toImmutable();
        final var blockData = buildingBlockData.entrySet().stream()
                .collect(Collectors.toMap(it -> it.getKey().add(start).toImmutable(), Map.Entry::getValue));
        this.floorLevel = floorLevel;
        this.buildingBlockData = new FortressBuildingBlockData(blockData, start.getY() + floorLevel);
        this.lastUpdated = LocalDateTime.MIN;
        this.blueprintId = blueprintId;
    }

    public FortressBuilding(NbtCompound tag) {
        if (tag.contains("id")) {
            this.id = tag.getUuid("id");
        } else {
            this.id = UUID.randomUUID();
        }

        if (tag.contains("start"))
            this.start = BlockPos.fromLong(tag.getLong("start"));
        else
            throw new IllegalArgumentException("Tag does not contain start");

        if (tag.contains("end"))
            this.end = BlockPos.fromLong(tag.getLong("end"));
        else
            throw new IllegalArgumentException("Tag does not contain end");

        if (tag.contains("lastUpdated"))
            this.lastUpdated = LocalDateTime.parse(tag.getString("lastUpdated"));
        else
            this.lastUpdated = LocalDateTime.MIN;

        if (tag.contains("blueprintId"))
            this.blueprintId = tag.getString("blueprintId");
        else if (tag.contains("file")) // support old format
            this.blueprintId = tag.getString("file");
        else
            this.blueprintId = null;

        if(tag.contains("buildingBlockData")) {
            final var buildBlockDataTag = tag.get("buildingBlockData");
            if(buildBlockDataTag != null && buildBlockDataTag.getType() == NbtElement.COMPOUND_TYPE) {
                this.buildingBlockData = FortressBuildingBlockData.fromNbt((NbtCompound) buildBlockDataTag);
            } else {
                this.buildingBlockData = null;
            }
        } else {
            this.buildingBlockData = null;
        }

        if (tag.contains("floorLevel"))
            this.floorLevel = tag.getInt("floorLevel");
        else
            this.floorLevel = 0;
    }

    @Override
    public boolean updateTheHealthState(ServerWorld world) {
        if(world == null || world.getRegistryKey() != World.OVERWORLD) {
            return false;
        }

        if(buildingBlockData == null) {
            final var blocks = BlockPos.stream(start, end)
                    .collect(Collectors.toMap(BlockPos::toImmutable, world::getBlockState));
            buildingBlockData = new FortressBuildingBlockData(blocks, this.start.getY() + floorLevel);
        }

        return buildingBlockData.checkTheNextBlocksState(MAX_BLOCKS_PER_UPDATE, world);
    }

    @Override
    public int getHealth() {
        return buildingBlockData == null ? 0 : buildingBlockData.getHealth();
    }

    @Override
    public boolean isPartOfTheBuilding(BlockPos pos) {
        return start.getX() <= pos.getX() && pos.getX() <= end.getX()
                && start.getY()-1 <= pos.getY() && pos.getY() <= end.getY() + 1
                && start.getZ() <= pos.getZ() && pos.getZ() <= end.getZ();
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
    public BlockPos getCenter() {
        return new BlockPos((start.getX() + end.getX()) / 2, (start.getY() + end.getY()) / 2, (start.getZ() + end.getZ()) / 2);
    }

    @Override
    public BlockPos getNearestCornerXZ(BlockPos pos, World world) {
        final var x = pos.getX() < start.getX() ? start.getX() : Math.min(pos.getX(), end.getX());
        final var z = pos.getZ() < start.getZ() ? start.getZ() : Math.min(pos.getZ(), end.getZ());
        return world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, 0, z));
    }

    @Override
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

    @Override
    public long getBedsCount(World world) {
        return streamBeds(world).count();
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putUuid("id", id);
        tag.putLong("start", start.asLong());
        tag.putLong("end", end.asLong());
        tag.putString("lastUpdated", lastUpdated.toString());
        if(blueprintId != null) {
            tag.putString("blueprintId", blueprintId);
        }
        if(buildingBlockData != null) {
            tag.put("buildingBlockData", buildingBlockData.toNbt());
        }
        tag.putInt("floorLevel", floorLevel);
    }

    @Override
    public Iterator<IAutomationBlockInfo> iterator(World world) {
        if (currentIterator == null || !currentIterator.hasNext()) {
            if (satisfiesRequirement(ProfessionType.FARMER, 0)) {
                this.currentIterator = new FarmBuildingIterator(start, end, world);
            }
        }
        if(this.currentIterator == null) {
            throw new IllegalStateException("Iterator is not set properly");
        }

        return currentIterator;
    }

    @Override
    public boolean satisfiesRequirement(ProfessionType type, int minLevel) {
        return type.getBlueprintIds().indexOf(blueprintId) >= minLevel;
    }

    @Override
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

    @Override
    public void attack(HostileEntity attacker) {
        if(buildingBlockData != null)
            if(buildingBlockData.attack(attacker)) {
                this.attackers.add(attacker);
            }
    }

    @Override
    public Set<HostileEntity> getAttackers() {
        attackers.removeIf(it -> !it.isAlive());
        return attackers;
    }

    @Override
    public IEssentialBuildingInfo toEssentialInfo(World world) {
        return new EssentialBuildingInfo(id, start, end, getBedsCount(world), blueprintId, getHealth());
    }

    @Override
    public Map<BlockPos, BlockState> getAllBlockStatesToRepairTheBuilding() {
        return buildingBlockData == null ? Collections.emptyMap() : buildingBlockData.getAllBlockStatesToRepairTheBuilding();
    }
}
