package org.minefortress.fortress.buildings;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
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
import net.remmintan.mods.minefortress.core.interfaces.blueprints.buildings.IServerBuildingsManager;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.ITickableManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundSyncBuildingsPacket;
import net.remmintan.mods.minefortress.networking.s2c.S2COpenBuildingRepairScreen;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FortressBuildingManager implements IAutomationAreaProvider, IServerBuildingsManager, ITickableManager, IWritableManager {

    private int buildingPointer = 0;
    private final List<BlockPos> buildings = new ArrayList<>();
    private final Supplier<ServerWorld> overworldSupplier;
    private final IServerFortressManager fortressManager;
    private final Cache<BlockPos, Object> bedsCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(10, TimeUnit.SECONDS)
                    .build();
    private boolean needSync = false;

    public FortressBuildingManager(Supplier<ServerWorld> worldSupplier, IServerFortressManager fortressManager) {
        this.overworldSupplier = worldSupplier;
        this.fortressManager = fortressManager;
    }

    private static @NotNull BlockPos getCenterTop(BlockBox blockBox) {
        final var center = blockBox.getCenter();
        final var ceilingY = blockBox.getMaxY() + 1;

        final var buildingPos = new BlockPos(center.getX(), ceilingY, center.getZ());
        return buildingPos;
    }

    public void addBuilding(BlueprintMetadata metadata, BlockPos start, BlockPos end, Map<BlockPos, BlockState> mergedBlockData) {
        final var blockBox = BlockBox.create(start, end);
        final var buildingPos = getCenterTop(blockBox);

        final var world = getWorld();
        world.setBlockState(buildingPos, FortressBlocks.FORTRESS_BUILDING.getDefaultState(), 3);
        final var blockEntity = world.getBlockEntity(buildingPos);
        if (blockEntity instanceof FortressBuildingBlockEntity b) {
            b.init(metadata, start, end, mergedBlockData);
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
        return getBuildingsStream().mapToLong(IFortressBuilding::getBedsCount).reduce(0, Long::sum);
    }

    public void tick(ServerPlayerEntity player) {
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
        final var requiredBuildings = getBuildingsStream()
                .filter(b -> b.satisfiesRequirement(type, level));
        if (
                type == ProfessionType.MINER ||
                        type == ProfessionType.LUMBERJACK ||
                        type == ProfessionType.WARRIOR
        ) {
            return requiredBuildings
                    .mapToLong(it -> it.getBedsCount() * 10L)
                    .sum() > minCount;
        }
        final var count = requiredBuildings.count();
        if (type == ProfessionType.ARCHER)
            return count * 10 > minCount;

        if (type == ProfessionType.FARMER)
            return count * 5 > minCount;

        if (type == ProfessionType.FISHERMAN)
            return count * 3 > minCount;

        return count > minCount;
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
        return getBuildingsStream()
                .filter(building -> building.satisfiesRequirement(type, 0))
                .map(IAutomationArea.class::cast);
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

    @Override
    public void doRepairConfirmation(BlockPos pos, ServerPlayerEntity player) {
        final var statesThatNeedsToBeRepaired = getBuilding(pos)
                .map(IFortressBuilding::getAllBlockStatesToRepairTheBuilding)
                .orElse(Collections.emptyMap());

        final var packet = new S2COpenBuildingRepairScreen(pos, statesThatNeedsToBeRepaired);
        FortressServerNetworkHelper.send(player, S2COpenBuildingRepairScreen.CHANNEL, packet);
    }

    @NotNull
    public Optional<IFortressBuilding> getBuilding(BlockPos pos) {
        final var blockEntity = getWorld().getBlockEntity(pos);
        return blockEntity instanceof IFortressBuilding b ? Optional.of(b) : Optional.empty();
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
        return this.overworldSupplier.get();
    }

}
