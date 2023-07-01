package org.minefortress.fortress.buildings;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.minefortress.fortress.automation.IAutomationArea;
import org.minefortress.fortress.automation.IAutomationAreaProvider;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.network.s2c.ClientboundSyncBuildingsPacket;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FortressBuildingManager implements IAutomationAreaProvider {

    private int buildingPointer = 0;
    private final List<FortressBuilding> buildings = new ArrayList<>();
    private final Supplier<ServerWorld> overworldSupplier;
    private boolean needSync = false;

    public FortressBuildingManager(Supplier<ServerWorld> overworldSupplier) {
        this.overworldSupplier = overworldSupplier;
    }

    public void addBuilding(FortressBuilding building) {
        buildings.add(building);
        this.scheduleSync();
    }

    public void destroyBuilding(UUID id) {
        buildings.stream()
                .filter(it -> it.getId().equals(id))
                .findFirst()
                .ifPresent(it -> {
                    buildings.remove(it);
                    BlockPos.iterate(it.getStart(), it.getEnd())
                            .forEach(pos -> getWorld().setBlockState(pos, Blocks.AIR.getDefaultState()));
                    this.scheduleSync();
                });
    }


    public Optional<BlockPos> getFreeBed(){
        final var freeBedsList = buildings
                .stream()
                .map(it -> it.getFreeBed(getWorld()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        if(freeBedsList.isEmpty())
            return Optional.empty();
        return Optional.of(freeBedsList.get(new Random().nextInt(freeBedsList.size())));
    }

    public long getTotalBedsCount() {
        return buildings.stream().mapToLong(it -> it.getBedsCount(getWorld())).reduce(0, Long::sum);
    }

    public void tick(ServerPlayerEntity player) {
        if(player != null) {
            if (needSync) {
                final var houses = buildings.stream()
                        .map(it -> it.toEssentialInfo(getWorld()))
                        .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
                final var syncBuildings = new ClientboundSyncBuildingsPacket(houses);
                FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_BUILDINGS_SYNC, syncBuildings);

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

    public boolean hasRequiredBuilding(String requirementId, int minCount) {
        final var requiredBuildings = buildings.stream()
                .filter(b -> b.satisfiesRequirement(requirementId));
        if(requirementId.startsWith("miner") || requirementId.startsWith("lumberjack") || requirementId.startsWith("warrior")) {
            return requiredBuildings
                    .mapToLong(it -> it.getBedsCount(getWorld()) * 10)
                    .sum() > minCount;
        }
        final var count = requiredBuildings.count();
        if(requirementId.equals("shooting_gallery"))
            return count * 10 > minCount;

        if(requirementId.startsWith("farm"))
            return count * 5 > minCount;
        return count > minCount;
    }

    public NbtCompound toNbt() {
        int i = 0;
        final NbtCompound buildingsTag = new NbtCompound();
        for (FortressBuilding building : this.buildings) {
            final NbtCompound buildingTag = new NbtCompound();
            building.writeToNbt(buildingTag);
            buildingsTag.put("building" + i++, buildingTag);
        }

        buildingsTag.putInt("buildingPointer", buildingPointer);

        return buildingsTag;
    }

    public void readFromNbt(NbtCompound buildingsTag) {
        int i = 0;
        while(buildingsTag.contains("building" + i)) {
            final NbtCompound buildingTag = buildingsTag.getCompound("building" + i++);
            FortressBuilding building = new FortressBuilding(buildingTag);
            buildings.add(building);
            this.scheduleSync();
        }

        buildingPointer = buildingsTag.getInt("buildingPointer");
    }

    @Override
    public Stream<IAutomationArea> getAutomationAreasByRequirement(String requirementId) {
        return this.buildings.stream()
                .filter(building -> building.satisfiesRequirement(requirementId))
                .map(IAutomationArea.class::cast);
    }

    public boolean isPartOfAnyBuilding(BlockPos pos) {
        return buildings.stream().anyMatch(it -> it.isPartOfTheBuilding(pos));
    }

    private ServerWorld getWorld() {
        return this.overworldSupplier.get();
    }
}
