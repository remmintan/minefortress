package org.minefortress.fortress;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.BasePawnEntity;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.colonist.ColonistNameGenerator;
import org.minefortress.entity.interfaces.IProfessional;
import org.minefortress.entity.interfaces.IWorkerPawn;
import org.minefortress.fortress.resources.FortressResourceManager;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.fortress.resources.server.ServerResourceManager;
import org.minefortress.fortress.resources.server.ServerResourceManagerImpl;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.network.s2c.ClientboundSyncBuildingsPacket;
import org.minefortress.network.s2c.ClientboundSyncFortressManagerPacket;
import org.minefortress.network.s2c.ClientboundSyncSpecialBlocksPacket;
import org.minefortress.professions.ServerProfessionManager;
import org.minefortress.registries.FortressEntities;
import org.minefortress.tasks.TaskManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public final class FortressServerManager extends AbstractFortressManager {

    private static final BlockState DEFAULT_STATE_ABOVE_CAMPFIRE = Blocks.BARRIER.getDefaultState();
    private static final int DEFAULT_COLONIST_COUNT = 5;
    
    private final MinecraftServer server;
    
    private final Set<LivingEntity> pawns = new HashSet<>();
    private final Set<FortressBuilding> buildings = new HashSet<>();

    private final Map<Block, List<BlockPos>> specialBlocks = new HashMap<>();
    private final Map<Block, List<BlockPos>> blueprintsSpecialBlocks = new HashMap<>();
    
    private final ServerProfessionManager serverProfessionManager;
    private final ServerResourceManager serverResourceManager;
    private final TaskManager taskManager = new TaskManager();
    
    private ColonistNameGenerator nameGenerator = new ColonistNameGenerator();

    private int maxX = Integer.MIN_VALUE;
    private int maxZ = Integer.MIN_VALUE;
    private int minX = Integer.MAX_VALUE;
    private int minZ = Integer.MAX_VALUE;

    private FortressGamemode gamemode = FortressGamemode.NONE;

    private boolean needSync = true;
    private boolean needSyncBuildings = true;
    private boolean needSyncSpecialBlocks = true;

    private BlockPos fortressCenter = null;
    private int maxColonistsCount = -1;

    public FortressServerManager(MinecraftServer server) {
        this.server = server;
        this.serverProfessionManager = new ServerProfessionManager(() -> this);
        this.serverResourceManager = new ServerResourceManagerImpl(server);
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            this.gamemode = FortressGamemode.SURVIVAL;
        }
    }

    public void addBuilding(FortressBuilding building) {
        final BlockPos start = building.getStart();
        final BlockPos end = building.getEnd();
        if(start.getX() < minX) minX = start.getX();
        if(start.getZ() < minZ) minZ = start.getZ();
        if(end.getX() > maxX) maxX = end.getX();
        if(end.getZ() > maxZ) maxZ = end.getZ();
        buildings.add(building);
        this.scheduleSyncBuildings();
    }

    public Optional<BlockPos> getFreeBed(){
        return buildings
                .stream()
                .map(it -> it.getFreeBed(server.getWorld(World.OVERWORLD)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
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

    public void tick(@Nullable ServerPlayerEntity player) {
        taskManager.tick(this, getWorld());
        tickFortress(player);
        serverProfessionManager.tick(player);
        serverResourceManager.tick(player);
        if(!needSync || player == null) return;
        final var packet = new ClientboundSyncFortressManagerPacket(pawns.size(), fortressCenter, gamemode, maxColonistsCount);
        FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_MANAGER_SYNC, packet);
        if (needSyncBuildings) {
            final var houses = buildings.stream()
                    .map(h -> new EssentialBuildingInfo(h.getStart(), h.getEnd(), h.getRequirementId(), h.getBedsCount(getWorld())))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
            final var syncBuildings = new ClientboundSyncBuildingsPacket(houses);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_BUILDINGS_SYNC, syncBuildings);
            needSyncBuildings = false;
        }
        if(needSyncSpecialBlocks){
            final var syncBlocks = new ClientboundSyncSpecialBlocksPacket(specialBlocks, blueprintsSpecialBlocks);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_SPECIAL_BLOCKS_SYNC, syncBlocks);
            needSyncSpecialBlocks = false;
        }
        needSync = false;
    }

    public Optional<FortressBuilding> getRandomBuilding(String requirementId, Random random) {
        final var buildings = this.buildings.stream()
                .filter(building -> building.getRequirementId().equals(requirementId))
                .toList();
        if(buildings.isEmpty()) return Optional.empty();
        return Optional.of(buildings.get(random.nextInt(buildings.size())));
    }

    public void replaceColonistWithWarrior(Colonist colonist, String warriorId) {
        final var pos = colonist.getBlockPos();
        final var world = (ServerWorld) colonist.getEntityWorld();
        final var masterId = colonist.getMasterId().orElseThrow(() -> new IllegalStateException("Colonist has no master!"));
        final var name = colonist.getName();

        final var infoTag = getColonistInfoTag(masterId);
        infoTag.putString("warriorId", warriorId);

        final var newWarrior = FortressEntities.WARRIOR_PAWN_ENTITY_TYPE.spawn(world, infoTag, name, null, pos, SpawnReason.EVENT, true, false);
        colonist.damage(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
        pawns.remove(colonist);
        pawns.add(newWarrior);
    }

    public void tickFortress(@Nullable ServerPlayerEntity player) {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            throw new IllegalStateException("Tick should not be called on server");
        }

        if(maxColonistsCount != -1 && getColonistsCount() > maxColonistsCount) {
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
                    serverProfessionManager.decreaseAmount(professionId);
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
            final var spawnFactor = MathHelper.clampedLerp(84, 99, colonistsCount / 50f);
            if(maxColonistsCount == -1 || colonistsCount < maxColonistsCount) {
                if(getWorld().getTime() % 100 == 0  && getWorld().random.nextInt(100) > spawnFactor) {
                    final long bedsCount = buildings.stream().mapToLong(it -> it.getBedsCount(getWorld())).reduce(0, Long::sum);
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
        if(randomSpawnPosition.getX() != fortressCenter.getX() && randomSpawnPosition.getZ() != fortressCenter.getZ()) {
            final var tag = getColonistInfoTag(masterPlayerId);
            final var colonistType = FortressEntities.COLONIST_ENTITY_TYPE;
            final var world = getWorld();
            final var spawnedPawn = colonistType.spawn(world, tag, null, null, randomSpawnPosition, SpawnReason.MOB_SUMMONED, true, false);
            return Optional.ofNullable(spawnedPawn);
        }
        return Optional.empty();
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

        this.scheduleSync();
    }

    private static NbtCompound getColonistInfoTag(UUID masterPlayerId) {
        final NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putUuid(BasePawnEntity.FORTRESS_ID_NBT_KEY, masterPlayerId);
        return nbtCompound;
    }

    private BlockPos getRandomSpawnPosition() {
        final var spawnX = fortressCenter.getX() + getWorld().random.nextInt(10) - 5;
        final var spawnZ = fortressCenter.getZ() + getWorld().random.nextInt(10) - 5;
        final var spawnY = getWorld().getTopY(Heightmap.Type.WORLD_SURFACE, spawnX, spawnZ);

        return new BlockPos(spawnX, spawnY, spawnZ);
    }

    private void scheduleSync() {
        needSync = true;
    }

    private void scheduleSyncBuildings() {
        needSyncBuildings = true;
        this.scheduleSync();
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

        if(!buildings.isEmpty()) {
            int i = 0;
            final NbtCompound buildingsTag = new NbtCompound();
            for (FortressBuilding building : this.buildings) {
                final NbtCompound buildingTag = new NbtCompound();
                building.writeToNbt(buildingTag);
                buildingsTag.put("building" + i++, buildingTag);
            }
            tag.put("buildings", buildingsTag);
        }

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
                FortressBuilding building = new FortressBuilding(buildingTag);
                buildings.add(building);
                this.scheduleSyncBuildings();
            }
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

        this.scheduleSync();
    }

    public ColonistNameGenerator getNameGenerator() {
        return nameGenerator;
    }

    public int getColonistsCount() {
        return pawns.size();
    }

    public BlockPos getFortressCenter() {
        return fortressCenter!=null?fortressCenter.toImmutable():null;
    }

    public Optional<BlockPos> randomSurfacePos(){
        if(minX == Integer.MAX_VALUE) return Optional.empty();

        int tires = 0;
        BlockPos fortressPos;
        boolean isFluid, isFluidAbove;
        do {
            int x = getWorld().random.nextInt(maxX - minX) + minX;
            int z = getWorld().random.nextInt(maxZ - minZ) + minZ;
            int y = getWorld().getTopY(Heightmap.Type.WORLD_SURFACE, x, z);

            fortressPos = new BlockPos(x, y, z);
            isFluid = getWorld().getBlockState(fortressPos).isOf(Blocks.WATER);
            isFluidAbove = getWorld().getBlockState(fortressPos.down()).isOf(Blocks.WATER);
            tires++;
        }while((isFluid || isFluidAbove) && tires < 10);

        if(isFluid || isFluidAbove) return Optional.empty();

        return Optional.of(fortressPos.up());
    }

    public Optional<BlockPos> getRandomPositionAroundCampfire() {
        final var fortressCenter = getFortressCenter();
        if(fortressCenter == null) return Optional.empty();

        final var random = getWorld().random;
        final int x = random.nextInt(getHomeOuterRadius() - getHomeInnerRadius()) + getHomeInnerRadius() * (random.nextBoolean()?1:-1);
        final int z = random.nextInt(getHomeOuterRadius() - getHomeInnerRadius()) + getHomeInnerRadius() * (random.nextBoolean()?1:-1);

        final var blockX = fortressCenter.getX() + x;
        final var blockZ = fortressCenter.getZ() + z;
        final var blockY = getWorld().getTopY(Heightmap.Type.WORLD_SURFACE, blockX, blockZ);

        return Optional.of(new BlockPos(blockX, blockY, blockZ));
    }

    public int getHomeOuterRadius() {
        return Math.max(getColonistsCount(), 5) * 4 / 5;
    }

    private int getHomeInnerRadius() {
        return Math.max(getColonistsCount(), 5) * 2 / 5;
    }



    @Override
    public boolean hasRequiredBuilding(String requirementId, int minCount) {
        if(requirementId.startsWith("miner") || requirementId.startsWith("lumberjack") || requirementId.startsWith("warrior")) {
            return buildings.stream()
                    .filter(b -> b.getRequirementId().equals(requirementId))
                    .mapToLong(it -> it.getBedsCount(getWorld()))
                    .sum() > minCount;
        }
        if(requirementId.equals("shooting_gallery"))
            minCount = 0;
        return buildings.stream().filter(b -> b.getRequirementId().equals(requirementId)).count() > minCount;
    }

    @Override
    public boolean hasRequiredBlock(Block block, boolean blueprint, int minCount) {
        if(blueprint)
            return blueprintsSpecialBlocks.getOrDefault(block, Collections.emptyList()).size() > minCount;
        else
            return this.specialBlocks.getOrDefault(block, Collections.emptyList()).size() > minCount;
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
        if(this.maxColonistsCount >= getColonistsCount()) {
            this.maxColonistsCount = -1;
        }
        this.scheduleSync();
    }

    public void decreaseMaxColonistsCount() {
        if(maxColonistsCount == -1)
            this.maxColonistsCount = getColonistsCount();

        this.maxColonistsCount--;

        if(this.maxColonistsCount <= 0)
            this.maxColonistsCount = 1;
        this.scheduleSync();
    }

}
