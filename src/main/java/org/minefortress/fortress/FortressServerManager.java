package org.minefortress.fortress;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.apache.logging.log4j.core.jmx.Server;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.colonist.ColonistNameGenerator;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.ClientboundSyncFortressManagerPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public final class FortressServerManager extends AbstractFortressManager {

    private static final int DEFAULT_COLONIST_COUNT = 5;

    private boolean needSync = true;

    private BlockPos fortressCenter = null;
    private final Set<Colonist> colonists = new HashSet<>();
    private final Set<FortressBulding> buildings = new HashSet<>();

    private ColonistNameGenerator nameGenerator = new ColonistNameGenerator();

    private int maxX = Integer.MIN_VALUE;
    private int maxZ = Integer.MIN_VALUE;
    private int minX = Integer.MAX_VALUE;
    private int minZ = Integer.MAX_VALUE;

    public void addBuilding(FortressBulding building) {
        final BlockPos start = building.getStart();
        final BlockPos end = building.getEnd();
        if(start.getX() < minX) minX = start.getX();
        if(start.getZ() < minZ) minZ = start.getZ();
        if(end.getX() > maxX) maxX = end.getX();
        if(end.getZ() > maxZ) maxZ = end.getZ();
        buildings.add(building);
    }

    public Optional<FortressBedInfo> getFreeBed(){
        for(FortressBulding building : buildings){
            final Optional<FortressBedInfo> freeBed = building.getFreeBed();
            if(freeBed.isPresent()) return freeBed;
        }
        return Optional.empty();
    }

    public void addColonist(Colonist colonist) {
        colonists.add(colonist);
        scheduleSync();
    }

    public void tick(ServerPlayerEntity player) {
        tickFortress();
        if(!needSync) return;
        final ClientboundSyncFortressManagerPacket packet = new ClientboundSyncFortressManagerPacket(colonists.size(), fortressCenter);
        FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_MANAGER_SYNC, packet);
        needSync = false;
    }

    public void tickFortress() {
        if(colonists.removeIf(colonist -> !colonist.isAlive()))
            scheduleSync();

        for (FortressBulding building : buildings) {
            building.tick();
        }
    }

    public void setupCenter(BlockPos fortressCenter, World world, ServerPlayerEntity player) {
        if(fortressCenter == null) throw new IllegalArgumentException("Center cannot be null");
        this.fortressCenter = fortressCenter;

        if(!(world instanceof ServerWorld serverWorld))
            throw new IllegalArgumentException("World must be a server world");

        world.setBlockState(fortressCenter, getStateForCampCenter(), 3);
        world.emitGameEvent(player, GameEvent.BLOCK_PLACE, fortressCenter);

        final NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putUuid("fortressUUID", ((FortressServerPlayerEntity)player).getFortressUuid());
        nbtCompound.putInt("centerX", fortressCenter.getX());
        nbtCompound.putInt("centerY", fortressCenter.getY());
        nbtCompound.putInt("centerZ", fortressCenter.getZ());

        if(minX > fortressCenter.getX()-10) minX = fortressCenter.getX()-10;
        if(minZ > fortressCenter.getZ()-10) minZ = fortressCenter.getZ()-10;
        if(maxX < fortressCenter.getX()+10) maxX = fortressCenter.getX()+10;
        if(maxZ < fortressCenter.getZ()+10) maxZ = fortressCenter.getZ()+10;

        EntityType<?> colonistType = EntityType.get("minefortress:colonist").orElseThrow();
        Iterable<BlockPos> spawnPlaces = BlockPos.iterateRandomly(world.random, DEFAULT_COLONIST_COUNT, fortressCenter, 3);
        for(BlockPos spawnPlace : spawnPlaces) {
            int spawnY = world.getTopY(Heightmap.Type.WORLD_SURFACE, spawnPlace.getX(), spawnPlace.getZ());
            BlockPos spawnPos = new BlockPos(spawnPlace.getX(), spawnY, spawnPlace.getZ());
            colonistType.spawn(serverWorld, nbtCompound, null, player, spawnPos, SpawnReason.MOB_SUMMONED, true, false);
        }

        this.scheduleSync();
    }


    private void scheduleSync() {
        needSync = true;
    }

    public void writeToNbt(NbtCompound tag) {
        if(fortressCenter != null) {
            tag.putInt("centerX", fortressCenter.getX());
            tag.putInt("centerY", fortressCenter.getY());
            tag.putInt("centerZ", fortressCenter.getZ());
        }

        tag.putInt("minX", minX);
        tag.putInt("minZ", minZ);
        tag.putInt("maxX", maxX);
        tag.putInt("maxZ", maxZ);

        if(!buildings.isEmpty()) {
            int i = 0;
            final NbtCompound buildingsTag = new NbtCompound();
            for (FortressBulding building : this.buildings) {
                final NbtCompound buildingTag = new NbtCompound();
                building.writeToNbt(buildingTag);
                buildingsTag.put("building" + i++, buildingTag);
            }
            tag.put("buildings", buildingsTag);
        }

        final NbtCompound nameGeneratorTag = new NbtCompound();
        this.nameGenerator.write(nameGeneratorTag);
        tag.put("nameGenerator", nameGeneratorTag);

    }

    public void readFromNbt(NbtCompound tag) {
        final int centerX = tag.getInt("centerX");
        final int centerY = tag.getInt("centerY");
        final int centerZ = tag.getInt("centerZ");
        if(centerX != 0 || centerY != 0 || centerZ != 0) {
            fortressCenter = new BlockPos(centerX, centerY, centerZ);
        }

        if(tag.contains("minX")) minX = tag.getInt("minX");
        if(tag.contains("minZ")) minZ = tag.getInt("minZ");
        if(tag.contains("maxX")) maxX = tag.getInt("maxX");
        if(tag.contains("maxZ")) maxZ = tag.getInt("maxZ");

        if(tag.contains("buildings")) {
            final NbtCompound buildingsTag = tag.getCompound("buildings");
            int i = 0;
            while(buildingsTag.contains("building" + i)) {
                final NbtCompound buildingTag = buildingsTag.getCompound("building" + i++);
                FortressBulding building = new FortressBulding(buildingTag);
                buildings.add(building);
            }
        }

        if(tag.contains("nameGenerator")) {
            final NbtCompound nameGeneratorTag = tag.getCompound("nameGenerator");
            this.nameGenerator = new ColonistNameGenerator(nameGeneratorTag);
        }

        this.scheduleSync();
    }

    public ColonistNameGenerator getNameGenerator() {
        return nameGenerator;
    }

    public int getColonistsCount() {
        return colonists.size();
    }

    public BlockPos getFortressCenter() {
        return fortressCenter;
    }

    public Optional<BlockPos> randomSurfacePos(ServerWorld world){
        if(minX == Integer.MAX_VALUE) return Optional.empty();

        int x = world.random.nextInt(maxX - minX) + minX;
        int z = world.random.nextInt(maxZ - minZ) + minZ;
        int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
        return Optional.of(new BlockPos(x, y, z));
    }
}
