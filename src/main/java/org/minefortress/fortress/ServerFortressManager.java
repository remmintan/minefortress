package org.minefortress.fortress;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.dtos.PawnSkin;
import net.remmintan.mods.minefortress.core.interfaces.automation.IAutomationAreaProvider;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IServerBuildingsManager;
import net.remmintan.mods.minefortress.core.interfaces.entities.IPawnNameGenerator;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IProfessional;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.ITargetedPawn;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.utils.ServerExtensionsKt;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundSyncFortressManagerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.BasePawnEntity;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.colonist.ColonistNameGenerator;
import org.minefortress.fortress.automation.areas.ServerAutomationAreaInfo;
import org.minefortress.professions.ServerProfessionManager;
import org.minefortress.registries.FortressEntities;
import org.minefortress.tasks.RepairBuildingTask;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.remmintan.mods.minefortress.core.interfaces.automation.ProfessionsSelectionType.QUARRY;

public final class ServerFortressManager implements IServerFortressManager {

    private static final int DEFAULT_COLONIST_COUNT = 5;

    private final MinecraftServer server;
    private final ServerWorld world;
    private final Set<LivingEntity> pawns = new HashSet<>();

    private IPawnNameGenerator nameGenerator = new ColonistNameGenerator();

    private int maxX = Integer.MIN_VALUE;
    private int maxZ = Integer.MIN_VALUE;
    private int minX = Integer.MAX_VALUE;
    private int minZ = Integer.MAX_VALUE;


    private boolean needSync = true;

    private final BlockPos fortressCenter;
    private int maxColonistsCount = -1;

    private boolean spawnPawns = true;
    private PawnSkin pawnsSkin = null;

    public ServerFortressManager(BlockPos fortressPos, ServerWorld world) {
        this.fortressCenter = fortressPos;
        this.world = world;
        this.server = world.getServer();
    }

    public void addPawn(LivingEntity colonist) {
        pawns.add(colonist);
        scheduleSync();
    }

    @Override
    public void setPawnsSkin(PawnSkin pawnsSkin) {
        this.pawnsSkin = pawnsSkin;
        this.pawns.forEach(it -> {
            if (it instanceof BasePawnEntity bpe) {
                bpe.setPawnSkin(pawnsSkin);
            }
        });
    }

    @Override
    public boolean isPawnsSkinSet() {
        return pawnsSkin != null;
    }

    @Override
    public void setSpawnPawns(boolean spawnPawns) {
        this.spawnPawns = spawnPawns;
    }

    private NbtCompound getColonistInfoTag() {
        final NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putLong(BasePawnEntity.FORTRESS_CENTER_BLOCK_KEY, fortressCenter.asLong());
        if (pawnsSkin != null)
            nbtCompound.putString(BasePawnEntity.PAWN_SKIN_NBT_KEY, pawnsSkin.name());
        return nbtCompound;
    }

    @Override
    public void spawnDebugEntitiesAroundCampfire(EntityType<? extends IFortressAwareEntity> entityType, int num, ServerPlayerEntity requester) {
        final var infoTag = getColonistInfoTag();

        for (int i = 0; i < num; i++) {
            final var spawnPosition = getRandomSpawnPosition();
            final var pawn = entityType.spawn(
                    getWorld(),
                    infoTag,
                    (it) -> {
                    },
                    spawnPosition,
                    SpawnReason.EVENT,
                    true,
                    false
            );
            if (pawn instanceof LivingEntity le)
                pawns.add(le);
        }
    }

    private @NotNull IServerManagersProvider getManagersProvider() {
        return ServerModUtils.getManagersProvider(server, fortressCenter).orElseThrow();
    }

    @Override
    public void tick(@Nullable final ServerPlayerEntity player) {
        tickFortress();

        if (!needSync || player == null) return;
        final var isServer = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
        final var syncFortressPacket = new ClientboundSyncFortressManagerPacket(
                pawns.size(),
                fortressCenter,
                isServer,
                maxColonistsCount,
                getReservedPawnsCount()
        );
        FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_MANAGER_SYNC, syncFortressPacket);
        needSync = false;
    }

    public void replaceColonistWithTypedPawn(LivingEntity colonist, String warriorId, EntityType<? extends LivingEntity> entityType) {
        final var pos = getRandomSpawnPosition();
        final var world = (ServerWorld) colonist.getEntityWorld();

        final var infoTag = getColonistInfoTag();
        infoTag.putString(ServerProfessionManager.PROFESSION_NBT_TAG, warriorId);

        colonist.damage(getOutOfWorldDamageSource(), Float.MAX_VALUE);
        pawns.remove(colonist);
        final var typedReplacement = entityType.spawn(world, infoTag, (it) -> {
        }, pos, SpawnReason.EVENT, true, false);
        pawns.add(typedReplacement);
        getManagersProvider().getFightManager().sync();
    }

    private void sendMessageToFortressOwner(String message) {
        final var owner = ServerExtensionsKt.getFortressOwner(server, fortressCenter);
        if (owner != null) {
            final var text = Text.of(message);
            owner.sendMessage(text);
        }
    }

    private void keepColonistsBelowMax() {
        if (maxColonistsCount != -1 && getTotalColonistsCount() > maxColonistsCount) {
            final var deltaColonists = Math.max(pawns.stream().filter(LivingEntity::isAlive).count() - maxColonistsCount, 0);

            pawns.stream()
                    .filter(LivingEntity::isAlive)
                    .limit(deltaColonists)
                    .forEach(it -> it.damage(getOutOfWorldDamageSource(), Integer.MAX_VALUE));
        }
    }

    public int getReservedPawnsCount() {
        return (int) getProfessionals()
                .stream()
                .filter(it -> it.getProfessionId().equals(Colonist.RESERVE_PROFESSION_ID))
                .count();
    }

    public void killAllPawns() {
        final var outOfWorldDamageSource = getOutOfWorldDamageSource();
        pawns.forEach(it -> it.damage(outOfWorldDamageSource, 40f));
    }

    private DamageSource getOutOfWorldDamageSource() {
        return world.getDamageSources().outOfWorld();
    }

    private Stream<IWorkerPawn> getWorkersStream() {
        return pawns
                .stream()
                .filter(IWorkerPawn.class::isInstance)
                .map(IWorkerPawn.class::cast);
    }

    private void tickFortress() {
        keepColonistsBelowMax();

        final var deadPawns = pawns.stream()
                .filter(is -> !is.isAlive()).toList();
        if (!deadPawns.isEmpty()) {
            for (LivingEntity pawn : deadPawns) {
                if (pawn instanceof IProfessional professional) {
                    final String professionId = professional.getProfessionId();
                    getManagersProvider().getProfessionsManager().decreaseAmount(professionId, true);
                }
                pawns.remove(pawn);
            }
            scheduleSync();
        }


        if (this.fortressCenter != null) {
            final var colonistsCount = this.pawns.size();
            final var spawnFactor = MathHelper.clampedLerp(82, 99, colonistsCount / 50f);
            if (spawnPawns && (maxColonistsCount == -1 || colonistsCount < maxColonistsCount)) {
                if (getWorld().getTime() % 100 == 0 && getWorld().random.nextInt(100) >= spawnFactor) {
                    final long bedsCount = getManagersProvider().getBuildingsManager().getTotalBedsCount();
                    if (colonistsCount < bedsCount || colonistsCount < DEFAULT_COLONIST_COUNT) {
                        spawnPawnNearCampfire()
                                .ifPresent(it -> sendMessageToFortressOwner(it.getName().getString() + " appeared in the village."));
                    }
                }
            }
        }
    }

    @Override
    public void spawnInitialPawns() {
        if (minX > this.fortressCenter.getX() - 10) minX = this.fortressCenter.getX() - 10;
        if (minZ > this.fortressCenter.getZ() - 10) minZ = this.fortressCenter.getZ() - 10;
        if (maxX < this.fortressCenter.getX() + 10) maxX = this.fortressCenter.getX() + 10;
        if (maxZ < this.fortressCenter.getZ() + 10) maxZ = this.fortressCenter.getZ() + 10;

        for (int i = 0; i < 5; i++) {
            spawnPawnNearCampfire();
        }

//        player.setSpawnPoint(getWorld().getRegistryKey(), player.getBlockPos(), 0, true, false);

        this.scheduleSync();
    }


    @Override
    public void jumpToCampfire(ServerPlayerEntity player) {
        if (fortressCenter == null) return;
        if (player.getWorld().getRegistryKey() != World.OVERWORLD) return;

        player.teleport(fortressCenter.getX() + 10, fortressCenter.getY() + 20, fortressCenter.getZ() + 10);
    }

    @Override
    public void teleportToCampfireGround(ServerPlayerEntity player) {
        if (fortressCenter == null) return;
        if (player.getWorld().getRegistryKey() != World.OVERWORLD) return;

        // Get a position on the ground near the campfire
        BlockPos groundPos = getRandomSpawnPosition();
        player.teleport(groundPos.getX(), groundPos.getY(), groundPos.getZ());
    }

    @Override
    public Optional<LivingEntity> spawnPawnNearCampfire() {
        final var randomSpawnPosition = getRandomSpawnPosition();

        final var tag = getColonistInfoTag();
        final var colonistType = FortressEntities.COLONIST_ENTITY_TYPE;
        final var world = getWorld();
        final var spawnedPawn = colonistType.spawn(world, tag, (it) -> {
        }, randomSpawnPosition, SpawnReason.MOB_SUMMONED, true, false);
        return Optional.ofNullable(spawnedPawn);
    }

    private BlockPos getRandomSpawnPosition() {
        int spawnX, spawnZ, spawnY;
        do {
            spawnX = fortressCenter.getX() + getWorld().random.nextInt(10) - 5;
            spawnZ = fortressCenter.getZ() + getWorld().random.nextInt(10) - 5;
            spawnY = getWorld().getTopY(Heightmap.Type.WORLD_SURFACE, spawnX, spawnZ);
        } while (spawnX == fortressCenter.getX() && spawnZ == fortressCenter.getZ());

        return new BlockPos(spawnX, spawnY, spawnZ);
    }

    @Override
    public void sync() {
        this.needSync = true;
    }


    @Override
    public void scheduleSync() {
        needSync = true;
    }

    @Override
    public Set<IProfessional> getProfessionals() {
        return pawns
                .stream()
                .filter(IProfessional.class::isInstance)
                .map(IProfessional.class::cast)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void write(NbtCompound tag) {
        tag.putInt("minX", minX);
        tag.putInt("minZ", minZ);
        tag.putInt("maxX", maxX);
        tag.putInt("maxZ", maxZ);

        if (pawnsSkin != null)
            tag.putString(BasePawnEntity.PAWN_SKIN_NBT_KEY, pawnsSkin.name());

        final NbtCompound nameGeneratorTag = new NbtCompound();
        this.nameGenerator.write(nameGeneratorTag);
        tag.put("nameGenerator", nameGeneratorTag);

        if (maxColonistsCount != -1) {
            tag.putInt("maxColonistsCount", maxColonistsCount);
        }

        tag.putBoolean("spawnPawns", spawnPawns);
    }

    @Override
    public void read(NbtCompound tag) {
        if (tag.contains("minX")) minX = tag.getInt("minX");
        if (tag.contains("minZ")) minZ = tag.getInt("minZ");
        if (tag.contains("maxX")) maxX = tag.getInt("maxX");
        if (tag.contains("maxZ")) maxZ = tag.getInt("maxZ");

        if (tag.contains(BasePawnEntity.PAWN_SKIN_NBT_KEY))
            pawnsSkin = PawnSkin.valueOf(tag.getString(BasePawnEntity.PAWN_SKIN_NBT_KEY));

        if (tag.contains("nameGenerator")) {
            final NbtCompound nameGeneratorTag = tag.getCompound("nameGenerator");
            this.nameGenerator = new ColonistNameGenerator(nameGeneratorTag);
        }

        if (tag.contains("maxColonistsCount")) {
            this.maxColonistsCount = tag.getInt("maxColonistsCount");
        }

        if (tag.contains("spawnPawns")) {
            this.spawnPawns = tag.getBoolean("spawnPawns");
        }

        this.scheduleSync();
    }

    @Override
    public Optional<IAutomationArea> getAutomationAreaByProfessionType(ProfessionType professionType) {
        final var buildingsManager = getManagersProvider().getBuildingsManager();
        final var automationAreaManager = getManagersProvider().getAutomationAreaManager();

        if (buildingsManager instanceof IAutomationAreaProvider provider) {
            final var buildings = provider.getAutomationAreaByProfessionType(professionType);
            final var areas = automationAreaManager.getByProfessionType(professionType);

            final var areaOpt = Stream
                    .concat(buildings, areas)
                    .min(Comparator.comparing(IAutomationArea::getUpdated));

            if (areaOpt.isPresent()) {
                final var area = areaOpt.get();
                if (area instanceof ServerAutomationAreaInfo saai && area.isEmpty(getWorld()) && saai.getAreaType() == QUARRY) {
                    saai.sendFinishMessage(it -> ServerExtensionsKt.sendMessageToFortressOwner(server, fortressCenter, it));
                    automationAreaManager.removeArea(area.getId());
                    return getAutomationAreaByProfessionType(professionType);
                } else {
                    return areaOpt;
                }
            } else {
                return areaOpt;
            }
        }
        return Optional.empty();
    }

    public IPawnNameGenerator getNameGenerator() {
        return nameGenerator;
    }

    public BlockPos getFortressCenter() {
        return fortressCenter != null ? fortressCenter.toImmutable() : null;
    }

    public boolean isPositionWithinFortress(BlockPos pos) {
        if (minX == Integer.MAX_VALUE) {
            return false;
        }

        return pos.getX() >= minX && pos.getX() <= maxX && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    public Optional<BlockPos> getRandomPositionAroundCampfire() {
        final var position = getFortressCenter();
        if (position == null) return Optional.empty();

        final var random = getWorld().random;

        final var radius = Math.sqrt(random.nextDouble());
        final var angle = random.nextDouble() * 2 * Math.PI;
        final var x = (int) Math.round(radius * Math.cos(angle) * getCampfireWarmRadius());
        final var z = (int) Math.round(radius * Math.sin(angle) * getCampfireWarmRadius());

        final var blockX = position.getX() + x;
        final var blockZ = position.getZ() + z;
        final var blockY = getWorld().getTopY(Heightmap.Type.WORLD_SURFACE, blockX, blockZ);

        return Optional.of(new BlockPos(blockX, blockY, blockZ));
    }

    @Override
    public @Nullable BlockPos getRandomFortressPosition() {
        if (fortressCenter == null || server == null || world == null) return null;
        return ServerModUtils.getManagersProvider(server, fortressCenter)
                .map(IServerManagersProvider::getBuildingsManager)
                .map(IServerBuildingsManager::getRandomPositionToGoTo)
                .or(this::getRandomPositionAroundCampfire)
                .orElse(null);
    }

    public double getCampfireWarmRadius() {
        return Math.max(Math.sqrt(getTotalColonistsCount()), 4);
    }

    @Override
    public boolean hasRequiredBuilding(ProfessionType type, int level, int minCount) {
        return getManagersProvider().getBuildingsManager().hasRequiredBuilding(type, level, minCount);
    }

    @Override
    public int getTotalColonistsCount() {
        return this.pawns.size();
    }

    public Optional<IProfessional> getPawnWithoutAProfession() {
        return pawns
                .stream()
                .filter(Colonist.class::isInstance)
                .map(IProfessional.class::cast)
                .filter(it -> it.getProfessionId().equals(Colonist.DEFAULT_PROFESSION_ID))
                .findAny();
    }

    public List<IWorkerPawn> getReadyWorkers() {
        return getWorkersStream()
                .filter(it -> Colonist.DEFAULT_PROFESSION_ID.equals(it.getProfessionId()))
                .filter(it -> it.getTaskControl().readyToTakeNewTask() && it.getAreaBasedTaskControl().readyToTakeNewTask())
                .toList();
    }

    @Override
    public List<ITargetedPawn> getAllTargetedPawns() {
        return pawns
                .stream()
                .filter(ITargetedPawn.class::isInstance)
                .map(ITargetedPawn.class::cast)
                .toList();
    }

    private ServerWorld getWorld() {
        return world;
    }

    @Override
    public void increaseMaxColonistsCount() {
        if (maxColonistsCount == -1) return;
        this.maxColonistsCount++;
        if (this.maxColonistsCount >= getTotalColonistsCount()) {
            this.maxColonistsCount = -1;
        }
        this.scheduleSync();
    }

    @Override
    public void decreaseMaxColonistsCount() {
        if (maxColonistsCount == -1)
            this.maxColonistsCount = getTotalColonistsCount();

        this.maxColonistsCount--;

        if (this.maxColonistsCount <= 0)
            this.maxColonistsCount = 1;
        this.scheduleSync();
    }

    public void expandTheVillage(BlockPos pos) {
        if (maxX < pos.getX()) maxX = pos.getX();
        if (minX > pos.getX()) minX = pos.getX();
        if (maxZ < pos.getZ()) maxZ = pos.getZ();
        if (minZ > pos.getZ()) minZ = pos.getZ();
    }

    @Override
    public void repairBuilding(ServerPlayerEntity player, BlockPos pos, List<Integer> selectedPawns) {
        final var buildingManager = getManagersProvider().getBuildingsManager();
        final var building = buildingManager.getBuilding(pos)
                .orElseThrow(() -> new IllegalStateException("Building not found"));

        final var repairStacks = building.getRepairItemInfos();
        final var blocksToRepair = building.getBlocksToRepair();

        final var task = new RepairBuildingTask(building.getStart(), building.getEnd(), blocksToRepair, repairStacks);
        getManagersProvider().getTaskManager().addTask(task, selectedPawns, player);

    }

}
