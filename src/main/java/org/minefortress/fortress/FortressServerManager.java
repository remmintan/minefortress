package org.minefortress.fortress;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.colonist.ColonistNameGenerator;
import org.minefortress.fight.ServerFightManager;
import org.minefortress.fortress.resources.FortressResourceManager;
import org.minefortress.fortress.resources.server.ServerResourceManager;
import org.minefortress.fortress.resources.server.ServerResourceManagerImpl;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.mixins.interfaces.FortressDimensionTypeMixin;
import org.minefortress.network.ClientboundSyncBuildingsPacket;
import org.minefortress.network.ClientboundSyncCombatStatePacket;
import org.minefortress.network.ClientboundSyncFortressManagerPacket;
import org.minefortress.network.ClientboundSyncSpecialBlocksPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.professions.ServerProfessionManager;

import java.util.*;
import java.util.stream.Collectors;

public final class FortressServerManager extends AbstractFortressManager {

    private static final BlockState DEFAULT_STATE_ABOVE_CAMPFIRE = Blocks.BARRIER.getDefaultState();
    private static final int DEFAULT_COLONIST_COUNT = 5;

    private boolean needSync = true;
    private boolean needSyncBuildings = true;
    private boolean needSyncSpecialBlocks = true;
    private boolean needSyncCombat = true;

    private BlockPos fortressCenter = null;
    private final Set<Colonist> colonists = new HashSet<>();
    private final Set<FortressBuilding> buildings = new HashSet<>();

    private final Map<Block, List<BlockPos>> specialBlocks = new HashMap<>();
    private final Map<Block, List<BlockPos>> blueprintsSpecialBlocks = new HashMap<>();

    private ColonistNameGenerator nameGenerator = new ColonistNameGenerator();
    private final ServerProfessionManager serverProfessionManager;
    private final ServerResourceManager serverResourceManager = new ServerResourceManagerImpl();
    private final ServerFightManager serverFightManager = new ServerFightManager();

    private int maxX = Integer.MIN_VALUE;
    private int maxZ = Integer.MIN_VALUE;
    private int minX = Integer.MAX_VALUE;
    private int minZ = Integer.MAX_VALUE;

    private FortressGamemode gamemode = FortressGamemode.NONE;

    private UUID id = UUID.randomUUID();

    private boolean combatMode;
    private boolean villageUnderAttack;

    public FortressServerManager() {
        serverProfessionManager = new ServerProfessionManager(() -> this);
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

    public Optional<FortressBedInfo> getFreeBed(){
        for(FortressBuilding building : buildings){
            final Optional<FortressBedInfo> freeBed = building.getFreeBed();
            if(freeBed.isPresent()) return freeBed;
        }
        return Optional.empty();
    }

    public void addColonist(Colonist colonist) {
        colonists.add(colonist);
        scheduleSync();
    }

    @Override
    public FortressResourceManager getResourceManager() {
        return serverResourceManager;
    }

    public void tick(ServerPlayerEntity player) {
        tickFortress(player, player.world);
        serverProfessionManager.tick(player);
        serverResourceManager.tick(player);
        serverFightManager.tick();
        if(!needSync) return;
        final ClientboundSyncFortressManagerPacket packet = new ClientboundSyncFortressManagerPacket(colonists.size(), fortressCenter, this.gamemode);
        FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_MANAGER_SYNC, packet);
        if (needSyncBuildings) {
            final ClientboundSyncBuildingsPacket syncBuildings = new ClientboundSyncBuildingsPacket(buildings);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_BUILDINGS_SYNC, syncBuildings);
            needSyncBuildings = false;
        }
        if(needSyncSpecialBlocks){
            final ClientboundSyncSpecialBlocksPacket syncBlocks = new ClientboundSyncSpecialBlocksPacket(specialBlocks, blueprintsSpecialBlocks);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_SPECIAL_BLOCKS_SYNC, syncBlocks);
            needSyncSpecialBlocks = false;
        }
        if(needSyncCombat) {
            final ClientboundSyncCombatStatePacket syncCombatState = new ClientboundSyncCombatStatePacket(combatMode);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_COMBAT_STATE_SYNC, syncCombatState);
            needSyncCombat = false;
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

    public Optional<FortressBedInfo> getRandomBed(Random random) {
        final var allBeds = this.buildings.stream()
                .flatMap(building -> building.getBeds().stream())
                .toList();
        if(allBeds.isEmpty()) return Optional.empty();
        return Optional.of(allBeds.get(random.nextInt(allBeds.size())));
    }

    public void tickFortress(ServerPlayerEntity player, World world) {
        final List<Colonist> deadColonists = colonists.stream()
                .filter(colonist -> !colonist.isAlive())
                .collect(Collectors.toList());

        if(!deadColonists.isEmpty()) {
            for(Colonist colonist : deadColonists) {
                final String professionId = colonist.getProfessionId();
                serverProfessionManager.decreaseAmount(professionId);
                colonists.remove(colonist);
            }
            scheduleSync();
        }

        for (FortressBuilding building : buildings) {
            building.tick();
        }

        if(!(specialBlocks.isEmpty() || blueprintsSpecialBlocks.isEmpty())  && world != null && world.getDimension() == FortressDimensionTypeMixin.getOverworld()) {
            boolean needSync = false;
            for(var entry : new HashSet<>(specialBlocks.entrySet())) {
                final var block = entry.getKey();
                final var positions = entry.getValue();
                needSync = needSync || positions.removeIf(pos -> world.getBlockState(pos).getBlock() != block);
                if (positions.isEmpty()) {
                    specialBlocks.remove(block);
                }
            }
            for (var entry : new HashSet<>(blueprintsSpecialBlocks.entrySet())) {
                final var block = entry.getKey();
                final var positions = entry.getValue();
                needSync = needSync || positions.removeIf(pos -> world.getBlockState(pos).getBlock() != block);
                if (positions.isEmpty()) {
                    blueprintsSpecialBlocks.remove(block);
                }
            }
            if(needSync) {
                scheduleSyncSpecialBlocks();
            }
        }

        if(this.fortressCenter != null) {
            final BlockState blockState = world.getBlockState(this.fortressCenter);
            if(blockState != Blocks.CAMPFIRE.getDefaultState()) {
                world.setBlockState(fortressCenter, getStateForCampCenter(), 3);
                world.emitGameEvent(player, GameEvent.BLOCK_PLACE, fortressCenter);
            }
            final BlockPos aboveTheCenter = this.fortressCenter.up();
            final BlockState blockStateAbove = world.getBlockState(aboveTheCenter);
            if(blockStateAbove != DEFAULT_STATE_ABOVE_CAMPFIRE) {
                world.setBlockState(aboveTheCenter, DEFAULT_STATE_ABOVE_CAMPFIRE, 3);
                world.emitGameEvent(player, GameEvent.BLOCK_PLACE, aboveTheCenter);
            }

            if(world.getTime() % 100 == 0  && world.random.nextInt(100) > 85) {
                final var colonistsCount = this.colonists.size();
                final var bedsCount = buildings.stream().map(FortressBuilding::getBedsCount).reduce(0, Integer::sum);
                if(colonistsCount < bedsCount || colonistsCount < DEFAULT_COLONIST_COUNT) {
                    final var colonistOpt = spawnPawnNearCampfire(player, world);
                    if(colonistOpt.isPresent()) {
                        final var colonist = colonistOpt.get();
                        player.sendMessage(new LiteralText(colonist.getName().asString()+" appeared in the village."), false);
                    }
                }
            }
        }
    }

    private Optional<Colonist> spawnPawnNearCampfire(ServerPlayerEntity player, World world) {
        final var randomSpawnPosition = getRandomSpawnPosition(world);
        if(randomSpawnPosition.getX() != fortressCenter.getX() && randomSpawnPosition.getZ() != fortressCenter.getZ()) {
            final var tag = getColonistInfoTag((FortressServerPlayerEntity) player);
            EntityType<?> colonistType = EntityType.get("minefortress:colonist").orElseThrow();
            final var spawnedPawn = (Colonist)colonistType.spawn((ServerWorld) world, tag, null, player, randomSpawnPosition, SpawnReason.MOB_SUMMONED, true, false);
            return Optional.ofNullable(spawnedPawn);
        }
        return Optional.empty();
    }

    public void setupCenter(BlockPos fortressCenter, World world, ServerPlayerEntity player) {
        if(fortressCenter == null) throw new IllegalArgumentException("Center cannot be null");
        this.fortressCenter = fortressCenter;

        if(!(world instanceof ServerWorld))
            throw new IllegalArgumentException("World must be a server world");

        world.setBlockState(fortressCenter, getStateForCampCenter(), 3);
        world.emitGameEvent(player, GameEvent.BLOCK_PLACE, fortressCenter);

        if(minX > this.fortressCenter.getX()-10) minX = this.fortressCenter.getX()-10;
        if(minZ > this.fortressCenter.getZ()-10) minZ = this.fortressCenter.getZ()-10;
        if(maxX < this.fortressCenter.getX()+10) maxX = this.fortressCenter.getX()+10;
        if(maxZ < this.fortressCenter.getZ()+10) maxZ = this.fortressCenter.getZ()+10;

        for (int i = 0; i < 5; i++) {
            spawnPawnNearCampfire(player, world);
        }

        this.scheduleSync();
    }

    private NbtCompound getColonistInfoTag(FortressServerPlayerEntity player) {
        final NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putUuid("fortressUUID", player.getFortressUuid());
        nbtCompound.putInt("centerX", this.fortressCenter.getX());
        nbtCompound.putInt("centerY", this.fortressCenter.getY());
        nbtCompound.putInt("centerZ", this.fortressCenter.getZ());

        return nbtCompound;
    }

    private BlockPos getRandomSpawnPosition(World world) {
        final var spawnX = fortressCenter.getX() + world.random.nextInt(10) - 5;
        final var spawnZ = fortressCenter.getZ() + world.random.nextInt(10) - 5;
        final var spawnY = world.getTopY(Heightmap.Type.WORLD_SURFACE, spawnX, spawnZ);

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

    private void scheduleSyncCombat() {
        needSyncCombat = true;
        this.scheduleSync();
    }

    public Set<Colonist> getColonists() {
        return Collections.unmodifiableSet(colonists);
    }

    public void writeToNbt(NbtCompound tag) {
        if(id != null) {
            tag.putUuid("id", id);
        }

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


        this.serverResourceManager.write(tag);
    }

    public void readFromNbt(NbtCompound tag) {
        if(tag.contains("id")) {
            this.id = tag.getUuid("id");
        }

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

        int tires = 0;
        BlockPos fortressPos;
        boolean isFluid, isFluidAbove;
        do {
            int x = world.random.nextInt(maxX - minX) + minX;
            int z = world.random.nextInt(maxZ - minZ) + minZ;
            int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);

            fortressPos = new BlockPos(x, y, z);
            isFluid = world.getBlockState(fortressPos).isOf(Blocks.WATER);
            isFluidAbove = world.getBlockState(fortressPos.down()).isOf(Blocks.WATER);
            tires++;
        }while((isFluid || isFluidAbove) && tires < 10);

        if(isFluid || isFluidAbove) return Optional.empty();

        return Optional.of(fortressPos);
    }

    @Override
    public boolean hasRequiredBuilding(String requirementId, int minCount) {
        if(requirementId.startsWith("miner") || requirementId.startsWith("lumberjack") || requirementId.startsWith("warrior")) {
            return buildings.stream()
                    .filter(b -> b.getRequirementId().equals(requirementId))
                    .mapToInt(FortressBuilding::getBedsCount)
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
        return this.colonists.size();
    }

    public ServerProfessionManager getServerProfessionManager() {
        return serverProfessionManager;
    }

    public List<Colonist> getFreeColonists() {
        return this.colonists.stream().filter(c -> !c.getTaskControl().hasTask()).collect(Collectors.toList());
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

    public boolean isCombatMode() {
        return combatMode;
    }

    public boolean isVillageUnderAttack() {
        return villageUnderAttack;
    }

    public void setCombatMode(boolean combatMode, boolean villageUnderAttack) {
        if(this.combatMode == combatMode) {
            this.villageUnderAttack = villageUnderAttack;
            return;
        }
        this.combatMode = combatMode;
        this.villageUnderAttack = villageUnderAttack;
        this.scheduleSyncCombat();
        for(Colonist colonist : this.colonists) {
            if(!colonist.getFightControl().isDefender()) continue;
            final var fightControl = colonist.getFightControl();
            if(this.combatMode) {
                fightControl.setMoveTarget(this.fortressCenter);
            } else {
                fightControl.reset();
            }
        }
    }

    public void selectColonists(List<Integer> selectedIds) {
        final var selectionManager = serverFightManager.getServerFightSelectionManager();
        if(selectedIds.isEmpty()){
            selectionManager.clearSelection();
            return;
        }

        final var selectedColonists = this.colonists.stream()
                .filter(c -> selectedIds.contains(c.getId()))
                .filter(c -> c.getFightControl().isDefender())
                .toList();
        selectionManager.selectColonists(selectedColonists);
    }

    public ServerFightManager getServerFightManager() {
        return serverFightManager;
    }

    public UUID getId() {
        return id;
    }
}
