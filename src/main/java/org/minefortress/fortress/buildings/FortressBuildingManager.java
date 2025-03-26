package org.minefortress.fortress.buildings;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.blocks.FortressBlocks;
import net.remmintan.mods.minefortress.blocks.building.FortressBuildingBlockEntity;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.automation.IAutomationAreaProvider;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IServerBuildingsManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundSyncBuildingsPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FortressBuildingManager implements IAutomationAreaProvider, IServerBuildingsManager {

    private int buildingPointer = 0;
    private final List<BlockPos> buildings = new ArrayList<>();
    private final BlockPos fortressPos;
    private ServerWorld world;
    private IServerFortressManager fortressManager;
    private final Cache<BlockPos, Object> bedsCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(10, TimeUnit.SECONDS)
                    .build();
    private boolean needSync = false;

    public FortressBuildingManager(BlockPos fortressPos) {
        this.fortressPos = fortressPos;
    }

    private static @NotNull BlockPos getCenterTop(BlockBox blockBox) {
        final var center = blockBox.getCenter();
        final var ceilingY = blockBox.getMaxY() + 1;

        return new BlockPos(center.getX(), ceilingY, center.getZ());
    }

    public void addBuilding(BlockPos owningFortress, BlueprintMetadata metadata, BlockPos start, BlockPos end, Map<BlockPos, BlockState> blockData) {
        final var blockBox = BlockBox.create(start, end);
        final var buildingPos = getCenterTop(blockBox);

        final var world = getWorld();
        world.setBlockState(buildingPos, FortressBlocks.FORTRESS_BUILDING.getDefaultState(), 3);
        final var blockEntity = world.getBlockEntity(buildingPos);
        if (blockEntity instanceof FortressBuildingBlockEntity b) {
            b.init(owningFortress, metadata, start, end, blockData);
        }

        fortressManager.expandTheVillage(start);
        fortressManager.expandTheVillage(end);

        buildings.add(buildingPos);
        this.scheduleSync();
    }

    @Override
    public void destroyBuilding(BlockPos pos) {
        if (buildings.remove(pos))
            this.scheduleSync();
        getBuilding(pos).ifPresent(IFortressBuilding::destroy);
    }

    public Optional<BlockPos> getFreeBed(){
        final var freeBed = getBuildingsStream()
                .map(it -> it.getFreeBed(getWorld()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(it -> !bedsCache.asMap().containsKey(it))
                .findFirst();

        freeBed.ifPresent(it -> bedsCache.put(it, new Object()));
        return freeBed;
    }

    public long getTotalBedsCount() {
        return getBuildingsStream().mapToLong(it -> it.getMetadata().getCapacity()).reduce(0, Long::sum);
    }

    @Override
    public void tick(@NotNull MinecraftServer server, @NotNull ServerWorld world, @Nullable ServerPlayerEntity player) {
        if (this.world == null) {
            this.world = world;
            ServerModUtils.getFortressManager(server, fortressPos).ifPresent(it -> this.fortressManager = it);
        }

        if(player != null) {
            if (needSync) {
                final var packet = new ClientboundSyncBuildingsPacket(buildings);
                FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_BUILDINGS_SYNC, packet);

                needSync = false;
            }
        }

        if(!buildings.isEmpty()) {
            buildingPointer = buildingPointer % buildings.size();
            final var pos = buildings.get(buildingPointer++);
            if (this.getBuilding(pos).isEmpty()) {
                buildings.remove(pos);
                this.scheduleSync();
            }
        }
    }

    private void scheduleSync() {
        needSync = true;
    }

    @Override
    public boolean hasRequiredBuilding(ProfessionType type, int level, int minCount) {
        return getBuildingsStream()
                .filter(b -> b.satisfiesRequirement(type, level))
                .mapToInt(it -> it.getMetadata().getCapacity())
                .sum() > minCount;
    }

    @Override
    public void write(NbtCompound tag) {
        final var buildingsNbt = this.toNbt();
        tag.put("buildings", buildingsNbt);
    }

    @Override
    public void read(NbtCompound tag) {
        this.reset();
        if(!tag.contains("buildings")) return;
        final var buildingsTag = tag.getCompound("buildings");
        this.readFromNbt(buildingsTag);
    }

    @Override
    public Stream<IAutomationArea> getAutomationAreaByProfessionType(ProfessionType type) {
        if (type != ProfessionType.FARMER) return Stream.empty();
        return getBuildingsStream()
                .filter(building -> building.satisfiesRequirement(type, 0))
                .map(IFortressBuilding::getAutomationArea)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    @Override
    public boolean isPartOfAnyBuilding(BlockPos pos) {
        return getBuildingsStream().anyMatch(it -> it.isPartOfTheBuilding(pos));
    }

    @Override
    public Optional<IFortressBuilding> findNearest(BlockPos pos) {
        return findNearest(pos, null);
    }

    @Override
    public Optional<IFortressBuilding> findNearest(BlockPos pos, ProfessionType type) {
        final Predicate<IFortressBuilding> buildingsFilter = type == null ?
                it -> true :
                it -> it.satisfiesRequirement(type, 0);

        return getBuildingsStream()
                .filter(buildingsFilter)
                .sorted(Comparator.comparing(it -> it.getCenter().getSquaredDistance(pos)))
                .filter(it -> it.getHealth() > 0)
                .findFirst();
    }

    @NotNull
    public Optional<IFortressBuilding> getBuilding(BlockPos pos) {
        final var blockEntity = getWorld().getBlockEntity(pos);
        return blockEntity instanceof IFortressBuilding b ? Optional.of(b) : Optional.empty();
    }

    @NotNull
    public List<IFortressBuilding> getBuildings(ProfessionType profession) {
        return getBuildingsStream()
                .filter(it -> it.satisfiesRequirement(profession, 0))
                .toList();
    }

    @Override
    public List<IFortressBuilding> getBuildings(ProfessionType type, int level) {
        return getBuildingsStream()
                .filter(it -> it.satisfiesRequirement(type, level))
                .toList();
    }

    private @NotNull Stream<IFortressBuilding> getBuildingsStream() {
        return buildings.stream()
                .map(this::getBuilding)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public Optional<HostileEntity> getRandomBuildingAttacker() {
        final var attackersList = getBuildingsStream()
                .map(IFortressBuilding::getAttackers)
                .flatMap(Collection::stream)
                .toList();
        if(attackersList.isEmpty())
            return Optional.empty();

        final var random = getWorld().random;
        return Optional.of(attackersList.get(random.nextInt(attackersList.size())));
    }

    private NbtCompound toNbt() {
        final var buildingsPositions = this.buildings.stream().map(BlockPos::asLong).toList();

        final NbtCompound buildingsTag = new NbtCompound();
        buildingsTag.putLongArray("buildingPositions", buildingsPositions);
        buildingsTag.putInt("buildingPointer", buildingPointer);
        return buildingsTag;
    }

    private void readFromNbt(NbtCompound buildingsTag) {
        Arrays
                .stream(buildingsTag.getLongArray("buildingPositions"))
                .mapToObj(BlockPos::fromLong)
                .forEach(buildings::add);
        buildingPointer = buildingsTag.getInt("buildingPointer");
    }

    private void reset() {
        buildings.clear();
        bedsCache.invalidateAll();
        buildingPointer = 0;
        this.scheduleSync();
    }

    private ServerWorld getWorld() {
        return world;
    }

}
