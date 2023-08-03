package org.minefortress.fortress;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.BasePawnEntity;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.colonist.ColonistNameGenerator;
import org.minefortress.entity.interfaces.IProfessional;
import org.minefortress.entity.interfaces.IWorkerPawn;
import org.minefortress.fight.influence.ServerInfluenceManager;
import org.minefortress.fortress.automation.IAutomationArea;
import org.minefortress.fortress.automation.areas.AreasServerManager;
import org.minefortress.fortress.buildings.FortressBuildingManager;
import org.minefortress.fortress.resources.FortressResourceManager;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.fortress.resources.server.ServerResourceManager;
import org.minefortress.fortress.resources.server.ServerResourceManagerImpl;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.network.s2c.ClientboundSyncFortressManagerPacket;
import org.minefortress.network.s2c.ClientboundSyncSpecialBlocksPacket;
import org.minefortress.network.s2c.ClientboundTaskExecutedPacket;
import org.minefortress.professions.ServerProfessionManager;
import org.minefortress.registries.FortressEntities;
import org.minefortress.tasks.RepairBuildingTask;
import org.minefortress.tasks.TaskManager;
import org.minefortress.utils.BlockInfoUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FortressServerManager extends AbstractFortressManager {

    private static final BlockState DEFAULT_STATE_ABOVE_CAMPFIRE = Blocks.BARRIER.getDefaultState();
    private static final int DEFAULT_COLONIST_COUNT = 5;
    
    private final MinecraftServer server;
    
    private final Set<LivingEntity> pawns = new HashSet<>();

    private final Map<Block, List<BlockPos>> specialBlocks = new HashMap<>();
    private final Map<Block, List<BlockPos>> blueprintsSpecialBlocks = new HashMap<>();
    
    private final ServerProfessionManager serverProfessionManager;
    private final ServerResourceManager serverResourceManager;
    private final FortressBuildingManager fortressBuildingManager;
    private final TaskManager taskManager = new TaskManager();
    private final AreasServerManager areasServerManager = new AreasServerManager();
    private final ServerInfluenceManager influenceManager = new ServerInfluenceManager(this);
    
    private ColonistNameGenerator nameGenerator = new ColonistNameGenerator();

    private int maxX = Integer.MIN_VALUE;
    private int maxZ = Integer.MIN_VALUE;
    private int minX = Integer.MAX_VALUE;
    private int minZ = Integer.MAX_VALUE;

    private FortressGamemode gamemode = FortressGamemode.NONE;

    private boolean needSync = true;
    private boolean needSyncSpecialBlocks = true;

    private BlockPos fortressCenter = null;
    private int maxColonistsCount = -1;

    public FortressServerManager(MinecraftServer server) {
        this.server = server;
        this.serverProfessionManager = new ServerProfessionManager(() -> this, server);
        this.serverResourceManager = new ServerResourceManagerImpl(server);
        this.fortressBuildingManager = new FortressBuildingManager(() -> server.getWorld(World.OVERWORLD));
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            this.gamemode = FortressGamemode.SURVIVAL;
        }
    }

    public void addColonist(LivingEntity colonist) {
        pawns.add(colonist);
        scheduleSync();
    }

    @Override
    public FortressResourceManager getResourceManager() {
        return serverResourceManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public ServerInfluenceManager getInfluenceManager() {
        return influenceManager;
    }

    public FortressBuildingManager getFortressBuildingManager() {
        return fortressBuildingManager;
    }

    public void tick(@Nullable ServerPlayerEntity player) {
        taskManager.tick(this, getWorld());
        tickFortress(player);
        serverProfessionManager.tick(player);
        serverResourceManager.tick(player);
        areasServerManager.tick(player);
        influenceManager.tick(player);
        fortressBuildingManager.tick(player);
        if(!needSync || player == null) return;
        final var isServer = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
        final var packet = new ClientboundSyncFortressManagerPacket(pawns.size(), fortressCenter, gamemode, isServer, maxColonistsCount, getReservedPawnsCount());
        FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_MANAGER_SYNC, packet);
        if(needSyncSpecialBlocks){
            final var syncBlocks = new ClientboundSyncSpecialBlocksPacket(specialBlocks, blueprintsSpecialBlocks);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_SPECIAL_BLOCKS_SYNC, syncBlocks);
            needSyncSpecialBlocks = false;
        }
        needSync = false;
    }

    public void replaceColonistWithTypedPawn(Colonist colonist, String warriorId, EntityType<? extends BasePawnEntity> entityType) {
        final var pos = getRandomSpawnPosition();
        final var world = (ServerWorld) colonist.getEntityWorld();
        final var masterId = colonist.getMasterId().orElseThrow(() -> new IllegalStateException("Colonist has no master!"));
        final var name = colonist.getName();

        final var infoTag = getColonistInfoTag(masterId);
        infoTag.putString(ServerProfessionManager.PROFESSION_NBT_TAG, warriorId);

        final var newWarrior = entityType.spawn(world, infoTag, name, null, pos, SpawnReason.EVENT, true, false);
        colonist.damage(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
        pawns.remove(colonist);
        pawns.add(newWarrior);
    }

    public void tickFortress(@Nullable ServerPlayerEntity player) {
        if(maxColonistsCount != -1 && getTotalColonistsCount() > maxColonistsCount) {
            final var deltaColonists = Math.max( pawns.stream().filter(LivingEntity::isAlive).count() - maxColonistsCount, 0);

            pawns.stream()
                    .filter(LivingEntity::isAlive)
                    .limit(deltaColonists)
                    .forEach(it -> it.damage(DamageSource.OUT_OF_WORLD, 40f));
        }

        final var deadPawns = pawns.stream()
                .filter(is -> !is.isAlive()).toList();
        if(!deadPawns.isEmpty()) {
            for(LivingEntity pawn : deadPawns) {
                if(pawn instanceof IProfessional professional) {
                    final String professionId = professional.getProfessionId();
                    serverProfessionManager.decreaseAmount(professionId, true);
                }
                pawns.remove(pawn);
            }
            scheduleSync();
        }

        if(allPawnsAreFree() && (!specialBlocks.containsKey(Blocks.CRAFTING_TABLE) || specialBlocks.get(Blocks.CRAFTING_TABLE).isEmpty())) {
            final var ii = new ItemInfo(Items.CRAFTING_TABLE, 1);
            if(!serverResourceManager.hasItems(Collections.singletonList(ii))) {
                serverResourceManager.increaseItemAmount(Items.CRAFTING_TABLE, 1);
            }
        }

        if(!(specialBlocks.isEmpty() || blueprintsSpecialBlocks.isEmpty())  && getWorld() != null && getWorld().getRegistryKey() == World.OVERWORLD) {
            boolean needSync = false;
            for(var entry : new HashSet<>(specialBlocks.entrySet())) {
                final var block = entry.getKey();
                final var positions = entry.getValue();
                needSync = needSync || positions.removeIf(pos -> getWorld().getBlockState(pos).getBlock() != block);
                if (positions.isEmpty()) {
                    specialBlocks.remove(block);
                }
            }
            for (var entry : new HashSet<>(blueprintsSpecialBlocks.entrySet())) {
                final var block = entry.getKey();
                final var positions = entry.getValue();
                needSync = needSync || positions.removeIf(pos -> getWorld().getBlockState(pos).getBlock() != block);
                if (positions.isEmpty()) {
                    blueprintsSpecialBlocks.remove(block);
                }
            }
            if(needSync) {
                scheduleSyncSpecialBlocks();
            }
        }

        if(this.fortressCenter != null) {
            final BlockState blockState = getWorld().getBlockState(this.fortressCenter);
            if(blockState != Blocks.CAMPFIRE.getDefaultState()) {
                getWorld().setBlockState(fortressCenter, getStateForCampCenter(), 3);
                if(player != null)
                    getWorld().emitGameEvent(player, GameEvent.BLOCK_PLACE, fortressCenter);
            }
            final BlockPos aboveTheCenter = this.fortressCenter.up();
            final BlockState blockStateAbove = getWorld().getBlockState(aboveTheCenter);
            if(blockStateAbove != DEFAULT_STATE_ABOVE_CAMPFIRE) {
                getWorld().setBlockState(aboveTheCenter, DEFAULT_STATE_ABOVE_CAMPFIRE, 3);
                if(player != null)
                    getWorld().emitGameEvent(player, GameEvent.BLOCK_PLACE, aboveTheCenter);
            }

            final var colonistsCount = this.pawns.size();
            final var spawnFactor = MathHelper.clampedLerp(82, 99, colonistsCount / 50f);
            if(maxColonistsCount == -1 || colonistsCount < maxColonistsCount) {
                if(getWorld().getTime() % 100 == 0  && getWorld().random.nextInt(100) >= spawnFactor) {
                    final long bedsCount = fortressBuildingManager.getTotalBedsCount();
                    if(colonistsCount < bedsCount || colonistsCount < DEFAULT_COLONIST_COUNT) {
                        if(player != null) {
                            spawnPawnNearCampfire(player.getUuid())
                                    .ifPresent(it -> player.sendMessage(new LiteralText(it.getName().asString()+" appeared in the village."), false));

                        }
                    }
                }
            }
        }
    }

    public int getReservedPawnsCount() {
        return (int) getProfessionals()
                .stream()
                .filter(it -> it.getProfessionId().equals(Colonist.RESERVE_PROFESSION_ID))
                .count();
    }

    public void killAllPawns() {
        pawns.forEach(it -> it.damage(DamageSource.OUT_OF_WORLD, 40f));
    }

    private Stream<IWorkerPawn> getWorkersStream() {
        return pawns
                .stream()
                .filter(IWorkerPawn.class::isInstance)
                .map(IWorkerPawn.class::cast);
    }

    private boolean allPawnsAreFree() {
        return getWorkersStream().noneMatch(it -> it.getTaskControl().hasTask());
    }

    public Optional<Colonist> spawnPawnNearCampfire(UUID masterPlayerId) {
        final var randomSpawnPosition = getRandomSpawnPosition();

        final var tag = getColonistInfoTag(masterPlayerId);
        final var colonistType = FortressEntities.COLONIST_ENTITY_TYPE;
        final var world = getWorld();
        final var spawnedPawn = colonistType.spawn(world, tag, null, null, randomSpawnPosition, SpawnReason.MOB_SUMMONED, true, false);
        return Optional.ofNullable(spawnedPawn);
    }

    public void setupCenter(@NotNull BlockPos fortressCenter, World world, ServerPlayerEntity player) {
        this.fortressCenter = fortressCenter;

        if(!(world instanceof ServerWorld))
            throw new IllegalArgumentException("World must be a server world");

        getWorld().setBlockState(fortressCenter, getStateForCampCenter(), 3);
        getWorld().emitGameEvent(player, GameEvent.BLOCK_PLACE, fortressCenter);

        if(minX > this.fortressCenter.getX()-10) minX = this.fortressCenter.getX()-10;
        if(minZ > this.fortressCenter.getZ()-10) minZ = this.fortressCenter.getZ()-10;
        if(maxX < this.fortressCenter.getX()+10) maxX = this.fortressCenter.getX()+10;
        if(maxZ < this.fortressCenter.getZ()+10) maxZ = this.fortressCenter.getZ()+10;

        for (int i = 0; i < 5; i++) {
            spawnPawnNearCampfire(player.getUuid());
        }

        influenceManager.addCenterAsInfluencePosition();
        player.setSpawnPoint(getWorld().getRegistryKey(), player.getBlockPos(), 0, true, false);

        this.scheduleSync();
    }

    public void jumpToCampfire(ServerPlayerEntity player) {
        if(fortressCenter == null) return;
        if(player.getWorld().getRegistryKey() != World.OVERWORLD) return;
        player.setPitch(60);
        player.setYaw(90 + 45);
        player.teleport(fortressCenter.getX() + 10, fortressCenter.getY() + 20, fortressCenter.getZ() + 10);
    }

    private static NbtCompound getColonistInfoTag(UUID masterPlayerId) {
        final NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putUuid(BasePawnEntity.FORTRESS_ID_NBT_KEY, masterPlayerId);
        return nbtCompound;
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

    public void syncOnJoin() {
        this.needSync = true;
        this.needSyncSpecialBlocks = true;
        final var resourceManager = (ServerResourceManager) this.getResourceManager();
        resourceManager.syncAll();
        areasServerManager.sync();
        influenceManager.sync();
    }

    public void scheduleSync() {
        needSync = true;
    }

    private void scheduleSyncSpecialBlocks() {
        needSyncSpecialBlocks = true;
        this.scheduleSync();
    }

    public Set<IProfessional> getProfessionals() {
        return pawns
                .stream()
                .filter(IProfessional.class::isInstance)
                .map(IProfessional.class::cast)
                .collect(Collectors.toUnmodifiableSet());
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

        final var buildingsNbt = fortressBuildingManager.toNbt();
        tag.put("buildings", buildingsNbt);

        final NbtCompound nameGeneratorTag = new NbtCompound();
        this.nameGenerator.write(nameGeneratorTag);
        tag.put("nameGenerator", nameGeneratorTag);

        if(!specialBlocks.isEmpty()) {
            final NbtCompound specialBlocksTag = new NbtCompound();
            for (var specialBlock : this.specialBlocks.entrySet()) {
                final String blockId = Registry.BLOCK.getId(specialBlock.getKey()).toString();
                final NbtList posList = new NbtList();
                for (BlockPos pos : specialBlock.getValue()) {
                    posList.add(NbtHelper.fromBlockPos(pos));
                }
                specialBlocksTag.put(blockId, posList);
            }
            tag.put("specialBlocks", specialBlocksTag);
        }

        if(!blueprintsSpecialBlocks.isEmpty()) {
            final NbtCompound blueprintsSpecialBlocksTag = new NbtCompound();
            for (var specialBlock : this.blueprintsSpecialBlocks.entrySet()) {
                final String blockId = Registry.BLOCK.getId(specialBlock.getKey()).toString();
                final NbtList posList = new NbtList();
                for (BlockPos pos : specialBlock.getValue()) {
                    posList.add(NbtHelper.fromBlockPos(pos));
                }
                blueprintsSpecialBlocksTag.put(blockId, posList);
            }
            tag.put("blueprintsSpecialBlocks", blueprintsSpecialBlocksTag);
        }

        NbtCompound professionTag = new NbtCompound();
        serverProfessionManager.writeToNbt(professionTag);
        tag.put("profession", professionTag);

        tag.putString("gamemode", this.gamemode.name());

        if(maxColonistsCount != -1) {
            tag.putInt("maxColonistsCount", maxColonistsCount);
        }

        this.serverResourceManager.write(tag);
        this.areasServerManager.write(tag);
        this.influenceManager.write(tag);
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

        fortressBuildingManager.reset();
        if(tag.contains("buildings")) {
            final NbtCompound buildingsTag = tag.getCompound("buildings");
            fortressBuildingManager.readFromNbt(buildingsTag);
        }

        if(tag.contains("nameGenerator")) {
            final NbtCompound nameGeneratorTag = tag.getCompound("nameGenerator");
            this.nameGenerator = new ColonistNameGenerator(nameGeneratorTag);
        }

        if (tag.contains("specialBlocks")) {
            final NbtCompound specialBlocksTag = tag.getCompound("specialBlocks");
            for (String blockId : specialBlocksTag.getKeys()) {
                final Block block = Registry.BLOCK.get(new Identifier(blockId));
                final NbtList posList = specialBlocksTag.getList(blockId, NbtElement.COMPOUND_TYPE);
                final var positions = new ArrayList<BlockPos>();
                for (int j = 0; j < posList.size(); j++) {
                    positions.add(NbtHelper.toBlockPos(posList.getCompound(j)));
                }
                this.specialBlocks.put(block, positions);
            }
            this.scheduleSyncSpecialBlocks();
        }

        if (tag.contains("blueprintsSpecialBlocks")) {
            final NbtCompound blueprintsSpecialBlocksTag = tag.getCompound("blueprintsSpecialBlocks");
            for (String blockId : blueprintsSpecialBlocksTag.getKeys()) {
                final Block block = Registry.BLOCK.get(new Identifier(blockId));
                final NbtList posList = blueprintsSpecialBlocksTag.getList(blockId, NbtElement.COMPOUND_TYPE);
                final var positions = new ArrayList<BlockPos>();
                for (int j = 0; j < posList.size(); j++) {
                    positions.add(NbtHelper.toBlockPos(posList.getCompound(j)));
                }
                this.blueprintsSpecialBlocks.put(block, positions);
            }
            this.scheduleSyncSpecialBlocks();
        }

        if (tag.contains("profession")) {
            NbtCompound professionTag = tag.getCompound("profession");
            serverProfessionManager.readFromNbt(professionTag);
        }

        if(tag.contains("gamemode")) {
            final String gamemodeName = tag.getString("gamemode");
            final FortressGamemode fortressGamemode = FortressGamemode.valueOf(gamemodeName);
            this.setGamemode(fortressGamemode);
        }

        this.serverResourceManager.read(tag);

        if(tag.contains("maxColonistsCount")) {
            this.maxColonistsCount = tag.getInt("maxColonistsCount");
        }

        this.areasServerManager.read(tag);
        this.influenceManager.read(tag);

        this.scheduleSync();
    }

    public Optional<IAutomationArea> getAutomationAreaByRequirementId(String requirement) {
        final var buildings = fortressBuildingManager.getAutomationAreasByRequirement(requirement);
        final var areas = areasServerManager.getByRequirement(requirement);

        return Stream
                .concat(buildings, areas)
                .min(Comparator.comparing(IAutomationArea::getUpdated));
    }

    public ColonistNameGenerator getNameGenerator() {
        return nameGenerator;
    }

    public BlockPos getFortressCenter() {
        return fortressCenter!=null?fortressCenter.toImmutable():null;
    }

    public Optional<BlockPos> getRandomPosWithinFortress(){
        if(minX == Integer.MAX_VALUE) return Optional.empty();

        final int x = getWorld().random.nextInt(maxX - minX) + minX;
        final int z = getWorld().random.nextInt(maxZ - minZ) + minZ;
        final int y = getWorld().getTopY(Heightmap.Type.WORLD_SURFACE, x, z);

        final BlockPos fortressPos = new BlockPos(x, y, z);
        if(fortressBuildingManager.isPartOfAnyBuilding(fortressPos)) return Optional.empty();
        boolean isFluid = getWorld().getBlockState(fortressPos).isOf(Blocks.WATER);
        if(isFluid) return Optional.empty();
        boolean isFluidAbove = getWorld().getBlockState(fortressPos.down()).isOf(Blocks.WATER);
        if(isFluidAbove) return Optional.empty();

        return Optional.of(fortressPos.up());
    }

    public boolean isPositionWithinFortress(BlockPos pos) {
        if(minX == Integer.MAX_VALUE) {
            return false;
        }

        return pos.getX() >= minX && pos.getX() <= maxX && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    public Optional<BlockPos> getRandomPositionAroundCampfire() {
        final var fortressCenter = getFortressCenter();
        if(fortressCenter == null) return Optional.empty();

        final var random = getWorld().random;

        final var radius = Math.sqrt(random.nextDouble());
        final var angle = random.nextDouble() * 2 * Math.PI;
        final var x = (int) Math.round(radius * Math.cos(angle) * getCampfireWarmRadius());
        final var z = (int) Math.round(radius * Math.sin(angle) * getCampfireWarmRadius());

        final var blockX = fortressCenter.getX() + x;
        final var blockZ = fortressCenter.getZ() + z;
        final var blockY = getWorld().getTopY(Heightmap.Type.WORLD_SURFACE, blockX, blockZ);

        return Optional.of(new BlockPos(blockX, blockY, blockZ));
    }

    public double getCampfireWarmRadius() {
        return Math.max(Math.sqrt(getTotalColonistsCount()), 4);
    }

    @Override
    public boolean hasRequiredBuilding(String requirementId, int minCount) {
        return fortressBuildingManager.hasRequiredBuilding(requirementId, minCount);
    }

    @Override
    public boolean hasRequiredBlock(Block block, boolean blueprint, int minCount) {
        if(blueprint)
            return blueprintsSpecialBlocks.getOrDefault(block, Collections.emptyList()).size() > minCount;
        else
            return this.specialBlocks.getOrDefault(block, Collections.emptyList()).size() > minCount;
    }

    public AreasServerManager getAreasManager() {
        return areasServerManager;
    }

    public boolean isBlockSpecial(Block block) {
        return block.equals(Blocks.CRAFTING_TABLE) || block.equals(Blocks.FURNACE);
    }

    public void addSpecialBlocks(Block block, BlockPos blockPos, boolean blueprint) {
        final var blocks = blueprint ?
                blueprintsSpecialBlocks.computeIfAbsent(block, k -> new ArrayList<>())
                :
                specialBlocks.computeIfAbsent(block, k -> new ArrayList<>());
        if(!blocks.contains(blockPos)) blocks.add(blockPos);
        scheduleSyncSpecialBlocks();
    }

    @Override
    public int getTotalColonistsCount() {
        return this.pawns.size();
    }

    public Optional<Colonist> getPawnWithoutAProfession() {
        return pawns
                .stream()
                .filter(Colonist.class::isInstance)
                .map(Colonist.class::cast)
                .filter(it -> it.getProfessionId().equals(Colonist.DEFAULT_PROFESSION_ID))
                .findAny();
    }

    public ServerProfessionManager getServerProfessionManager() {
        return serverProfessionManager;
    }

    public List<IWorkerPawn> getFreeColonists() {
        return getWorkersStream().filter(c -> !c.getTaskControl().hasTask()).collect(Collectors.toList());
    }

    public List<BlockPos> getSpecialBlocksByType(Block block, boolean blueprint) {
        if(blueprint)
            return blueprintsSpecialBlocks.getOrDefault(block, Collections.emptyList());
        else
            return specialBlocks.getOrDefault(block, Collections.emptyList());
    }

    @Override
    public void setGamemode(FortressGamemode gamemode) {
        this.gamemode = gamemode;
        this.scheduleSync();
    }

    @Override
    public boolean isCreative() {
        return gamemode == FortressGamemode.CREATIVE;
    }

    public boolean isSurvival() {
        return gamemode != null && gamemode == FortressGamemode.SURVIVAL;
    }

    public ServerResourceManager getServerResourceManager() {
        return serverResourceManager;
    }

    private ServerWorld getWorld() {
        return this.server.getWorld(World.OVERWORLD);
    }

    public void increaseMaxColonistsCount() {
        if(maxColonistsCount == -1) return;
        this.maxColonistsCount++;
        if(this.maxColonistsCount >= getTotalColonistsCount()) {
            this.maxColonistsCount = -1;
        }
        this.scheduleSync();
    }

    public void decreaseMaxColonistsCount() {
        if(maxColonistsCount == -1)
            this.maxColonistsCount = getTotalColonistsCount();

        this.maxColonistsCount--;

        if(this.maxColonistsCount <= 0)
            this.maxColonistsCount = 1;
        this.scheduleSync();
    }

    public void expandTheVillage(BlockPos pos) {
        if(maxX < pos.getX()) maxX = pos.getX();
        if(minX > pos.getX()) minX = pos.getX();
        if(maxZ < pos.getZ()) maxZ = pos.getZ();
        if(minZ > pos.getZ()) minZ = pos.getZ();
    }

    public double getVillageRadius() {
        final var radius1 = flatDistanceToCampfire(maxX, maxZ);
        final var radius2 = flatDistanceToCampfire(minX, minZ);
        final var radius3 = flatDistanceToCampfire(maxX, minZ);
        final var radius4 = flatDistanceToCampfire(minX, maxZ);

        return Math.max(Math.max(radius1, radius2), Math.max(radius3, radius4));
    }

    public void repairBuilding(ServerPlayerEntity player, UUID taskId, UUID buildingId) {
        final var buildingManager = getFortressBuildingManager();
        final var resourceManager = getServerResourceManager();

        try {
            final var building = buildingManager.getBuildingById(buildingId)
                    .orElseThrow(() -> new IllegalStateException("Building not found"));

            final var blocksToRepair = building.getAllBlockStatesToRepairTheBuilding();

            if(this.isSurvival()) {
                final var blockInfos = BlockInfoUtils.convertBlockStatesMapItemsMap(blocksToRepair)
                        .entrySet()
                        .stream()
                        .map(it -> new ItemInfo(it.getKey(), it.getValue().intValue()))
                        .toList();
                resourceManager.reserveItems(taskId, blockInfos);
            }

            final var task = new RepairBuildingTask(taskId, building.getStart(), building.getEnd(), blocksToRepair);
            taskManager.addTask(task, this);
        } catch (RuntimeException exp) {
            LogManager.getLogger().error("Error while repairing building", exp);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FINISH_TASK, new ClientboundTaskExecutedPacket(taskId));
        }
    }

    private double flatDistanceToCampfire(double x, double z) {
        final var campfireX = fortressCenter.getX();
        final var campfireZ = fortressCenter.getZ();

        final var deltaX = x - campfireX;
        final var deltaZ = z - campfireZ;

        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    }

}
