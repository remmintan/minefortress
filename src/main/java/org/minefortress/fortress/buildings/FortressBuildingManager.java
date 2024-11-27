package org.minefortress.fortress.buildings;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.automation.IAutomationAreaProvider;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.buildings.IServerBuildingsManager;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding;
import net.remmintan.mods.minefortress.core.interfaces.server.ITickableManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.S2COpenBuildingRepairScreen;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FortressBuildingManager implements IAutomationAreaProvider, IServerBuildingsManager, ITickableManager, IWritableManager {

    private int buildingPointer = 0;
    private final List<IFortressBuilding> buildings = new ArrayList<>();
    private final Supplier<ServerWorld> overworldSupplier;
    private final Cache<BlockPos, Object> bedsCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(10, TimeUnit.SECONDS)
                    .build();
    private boolean needSync = false;

    public FortressBuildingManager(Supplier<ServerWorld> overworldSupplier) {
        this.overworldSupplier = overworldSupplier;
    }

    public void addBuilding(IFortressBuilding building) {
        buildings.add(building);
        this.scheduleSync();
    }

    @Override
    public void destroyBuilding(UUID id) {
        getBuildingById(id)
                .ifPresent(it -> {
                    buildings.remove(it);
                    BlockPos.iterate(it.getStart(), it.getEnd())
                            .forEach(pos -> getWorld().setBlockState(pos, Blocks.AIR.getDefaultState()));
                    this.scheduleSync();
                });
    }

    public Optional<BlockPos> getFreeBed(){
        final var freeBed = buildings
                .stream()
                .map(it -> it.getFreeBed(getWorld()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(it -> !bedsCache.asMap().containsKey(it))
                .findFirst();

        freeBed.ifPresent(it -> bedsCache.put(it, new Object()));
        return freeBed;
    }

    public long getTotalBedsCount() {
        return buildings.stream().mapToLong(it -> it.getBedsCount(getWorld())).reduce(0, Long::sum);
    }

    public void tick(ServerPlayerEntity player) {
        if(player != null) {
            if (needSync) {
//                final var houses = buildings.stream()
//                        .map(it -> it.toEssentialInfo(getWorld()))
//                        .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
//                final var syncBuildings = new ClientboundSyncBuildingsPacket(houses);
//                FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_BUILDINGS_SYNC, syncBuildings);

                needSync = false;
            }
        }

        if(!buildings.isEmpty()) {
            buildingPointer = buildingPointer % buildings.size();
            final var building = buildings.get(buildingPointer++);
            if(building.updateTheHealthState(getWorld())) {
                this.scheduleSync();
            }

            if(building.getHealth() < 1) {
                this.destroyBuilding(building.getId());
            }
        }
    }

    private void scheduleSync() {
        needSync = true;
    }

    @Override
    public boolean hasRequiredBuilding(ProfessionType type, int level, int minCount) {
        final var requiredBuildings = buildings.stream()
                .filter(b -> b.satisfiesRequirement(type, level));
        if (
                type == ProfessionType.MINER ||
                        type == ProfessionType.LUMBERJACK ||
                        type == ProfessionType.WARRIOR
        ) {
            return requiredBuildings
                    .mapToLong(it -> it.getBedsCount(getWorld()) * 10L)
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
        return this.buildings.stream()
                .filter(building -> building.satisfiesRequirement(type, 0))
                .map(IAutomationArea.class::cast);
    }

    public boolean isPartOfAnyBuilding(BlockPos pos) {
        return buildings.stream().anyMatch(it -> it.isPartOfTheBuilding(pos));
    }

    public Optional<IFortressBuilding> findNearest(BlockPos pos) {
        return findNearest(pos, null);
    }

    public Optional<IFortressBuilding> findNearest(BlockPos pos, ProfessionType type) {
        final Predicate<IFortressBuilding> buildingsFilter = type == null ?
                it -> true :
                it -> it.satisfiesRequirement(type, 0);

        return buildings
                .stream()
                .filter(buildingsFilter)
                .sorted(Comparator.comparing(it -> it.getCenter().getSquaredDistance(pos)))
                .filter(it -> it.getHealth() > 0)
                .findFirst();
    }

    @Override
    public void doRepairConfirmation(UUID id, ServerPlayerEntity player) {
        final var statesThatNeedsToBeRepaired = getBuildingById(id)
                .map(IFortressBuilding::getAllBlockStatesToRepairTheBuilding)
                .orElse(Collections.emptyMap());

        final var packet = new S2COpenBuildingRepairScreen(id, statesThatNeedsToBeRepaired);
        FortressServerNetworkHelper.send(player, S2COpenBuildingRepairScreen.CHANNEL, packet);
    }

    @NotNull
    public Optional<IFortressBuilding> getBuildingById(UUID id) {
        return buildings.stream()
                .filter(it -> it.getId().equals(id))
                .findFirst();
    }

    public Optional<HostileEntity> getRandomBuildingAttacker() {
        final var attackersList = this.buildings
                .stream()
                .map(IFortressBuilding::getAttackers)
                .flatMap(Collection::stream)
                .toList();
        if(attackersList.isEmpty())
            return Optional.empty();

        final var random = getWorld().random;
        return Optional.of(attackersList.get(random.nextInt(attackersList.size())));
    }

    private NbtCompound toNbt() {
        int i = 0;
        final NbtCompound buildingsTag = new NbtCompound();
        for (IFortressBuilding building : this.buildings) {
            final NbtCompound buildingTag = new NbtCompound();
//            building.writeToNbt(buildingTag);
            buildingsTag.put("building" + i++, buildingTag);
        }

        buildingsTag.putInt("buildingPointer", buildingPointer);

        return buildingsTag;
    }

    private void readFromNbt(NbtCompound buildingsTag) {
        int i = 0;
        while(buildingsTag.contains("building" + i)) {
            final NbtCompound buildingTag = buildingsTag.getCompound("building" + i++);
            FortressBuilding building = new FortressBuilding(buildingTag);
            buildings.add(building);
            this.scheduleSync();
        }

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
