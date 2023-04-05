package org.minefortress.blueprints.data;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.Structure;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.MineFortressMod;
import org.minefortress.data.FortressModDataLoader;
import org.minefortress.network.s2c.ClientboundAddBlueprintPacket;
import org.minefortress.network.s2c.ClientboundUpdateBlueprintPacket;
import org.minefortress.network.interfaces.FortressS2CPacket;
import org.minefortress.renderer.gui.blueprints.BlueprintGroup;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ServerBlueprintBlockDataManager extends AbstractBlueprintBlockDataManager{

    private static final String BLUEPRINTS_FOLDER = "blueprints";
    private static final String REMOVED_BLUEPRINTS_FILENAME = "removed_blueprints.nbt";

    private final MinecraftServer server;
    private final Map<String, Blueprint> updatedStructures = new HashMap<>();
    private final Set<String> removedDefaultStructures = new HashSet<>();
    private final Function<String, Optional<BlueprintGroup>> filenameToGroupConverter;
    private final Supplier<UUID> userIdProvider;

    public ServerBlueprintBlockDataManager(MinecraftServer server, Function<String, Optional<BlueprintGroup>> filenameToGroupConverter, Supplier<UUID> userIdProvider) {
        this.server = server;
        this.filenameToGroupConverter = filenameToGroupConverter;
        this.userIdProvider = userIdProvider;
    }

    public Optional<Integer> getFloorLevel(String filename) {
        return Optional.ofNullable(updatedStructures.get(filename)).map(Blueprint::floorLevel);
    }

    public Optional<NbtCompound> getStructureNbt(String fileName) {
        return getStructure(fileName).map(it -> {
            NbtCompound compound = new NbtCompound();
            it.writeNbt(compound);
            return compound;
        });
    }

    public boolean update(String fileName, NbtCompound tag, int newFloorLevel, BlueprintGroup group) {
        if(group == null) {
            throw new IllegalArgumentException("Group can't be null");
        }
        final var alreadyIn = updatedStructures.containsKey(fileName);
        updatedStructures.put(fileName, new Blueprint(fileName, newFloorLevel, tag, group));
        removedDefaultStructures.remove(fileName);
        invalidateBlueprint(fileName);

        final var defaultStructure = filenameToGroupConverter.apply(fileName).isPresent();
        return alreadyIn || defaultStructure;
    }

    public List<FortressS2CPacket> getInitPackets() {
        return updatedStructures.values()
                .stream()
                .map(it -> {
                    final var group = filenameToGroupConverter.apply(it.filename);
                    if(group.isPresent()) {
                        return ClientboundUpdateBlueprintPacket.edit(it.filename, it.floorLevel, it.tag);
                    } else {
                        return new ClientboundAddBlueprintPacket(it.group, it.filename, it.filename, it.floorLevel, "custom", it.tag);
                    }
                })
                .toList();
    }

    public void remove(String fileName) {
        updatedStructures.remove(fileName);
        if(filenameToGroupConverter.apply(fileName).isPresent()) {
            removedDefaultStructures.add(fileName);
        }
    }

    @Override
    protected Optional<Structure> getStructure(String blueprintFileName) {
        if(removedDefaultStructures.contains(blueprintFileName)) {
            return Optional.empty();
        }
        if(updatedStructures.containsKey(blueprintFileName)) {
            final NbtCompound structureTag = updatedStructures.get(blueprintFileName).tag();
            final Structure structure = new Structure();
            structure.readNbt(structureTag);
            return Optional.of(structure);
        } else {
            return getDefaultStructure(blueprintFileName);
        }
    }

    private Optional<Structure> getDefaultStructure(String blueprintFileName) {
        final Identifier id;
        try {
            id = new Identifier(MineFortressMod.MOD_ID, blueprintFileName);
        }catch (InvalidIdentifierException exp) {
            return Optional.empty();
        }
        return server
                .getStructureManager()
                .getStructure(id);
    }

    @Override
    protected BlueprintBlockData buildBlueprint(Structure structure, BlockRotation rotation, int floorLevel) {
        final var sizeAndPivot = getSizeAndPivot(structure, rotation);
        final var size = sizeAndPivot.size();
        final var pivot = sizeAndPivot.pivot();

        final Map<BlockPos, BlockState> structureData = getStrcutureData(structure, rotation, pivot);

        final Map<BlockPos, BlockState> structureEntityData = structureData.entrySet()
            .stream()
            .filter(entry -> entry.getValue().getBlock() instanceof BlockEntityProvider || !entry.getValue().getFluidState().isEmpty())
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        final Map<BlockPos, BlockState> allBlocksWithoutEntities = structureData.entrySet()
            .stream()
            .filter(entry -> !(entry.getValue().getBlock() instanceof BlockEntityProvider) && entry.getValue().getFluidState().isEmpty())
            .filter(entry -> {
                final BlockPos pos = entry.getKey();
                final BlockState state = entry.getValue();
                return pos.getY() < floorLevel || !state.isAir();
            })
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        final Map<BlockPos, BlockState> manualData = new HashMap<>();
        final Map<BlockPos, BlockState> automaticData = new HashMap<>();

        for(int x = 0; x < size.getX(); x++) {
            for (int z = 0; z < size.getZ(); z++) {
                boolean isManual = true;
                for (int y = 0; y < size.getY(); y++) {
                    final BlockPos pos = new BlockPos(x, y, z);
                    boolean contains = allBlocksWithoutEntities.containsKey(pos);
                    if(!contains && y >= floorLevel) {
                        isManual = false;
                        continue;
                    }

                    if(isManual) {
                        if(contains)
                            manualData.put(pos, allBlocksWithoutEntities.get(pos));
                        else if(structureEntityData.containsKey(pos)) {
                            manualData.put(pos, Blocks.AIR.getDefaultState());
                        }
                    } else {
                        automaticData.put(pos, allBlocksWithoutEntities.get(pos));
                    }
                }
            }
        }

        return BlueprintBlockData
                .withBlueprintSize(size)
                .setLayer(BlueprintDataLayer.GENERAL, structureData)
                .setLayer(BlueprintDataLayer.MANUAL, manualData)
                .setLayer(BlueprintDataLayer.AUTOMATIC, automaticData)
                .setLayer(BlueprintDataLayer.ENTITY, structureEntityData)
                .build();
    }

    public void writeBlockDataManager() {
        FortressModDataLoader.clearFolder(getBlueprintsFolder(), server.session);
        FortressModDataLoader.createFolder(getBlueprintsFolder(), server.session);
        saveRemovedBlueprints();

        if(updatedStructures.isEmpty()) return;
        final var tags = new HashMap<String, NbtCompound>();
        updatedStructures.forEach((k, v) -> {
            final var tagFileName = getBlueprintsFolder() + "/" + k + ".nbt";
            final var tag = v.toNbt();
            tags.put(tagFileName, tag);
        });
        FortressModDataLoader.writeAllTags(tags, server.session);
    }

    @NotNull
    public String getBlueprintsFolder() {
        return BLUEPRINTS_FOLDER + "/" + this.userIdProvider.get();
    }

    private void saveRemovedBlueprints() {
        final var removedBlueprintsTag = new NbtCompound();
        final var removedStructs = String.join(":", removedDefaultStructures);
        removedBlueprintsTag.putString("removedDefaultBlueprints", removedStructs);
        FortressModDataLoader.saveNbt(removedBlueprintsTag, getRemovedBlueprintsDefaultFileName(), server.session);
    }

    @NotNull
    private String getRemovedBlueprintsDefaultFileName() {
        return  getBlueprintsFolder() +"/"+REMOVED_BLUEPRINTS_FILENAME;
    }

    public void readBlockDataManager(@Nullable NbtCompound tag) {
        if(FortressModDataLoader.exists(getBlueprintsFolder(), server.session)) {
            updatedStructures.clear();
            removedDefaultStructures.clear();
            FortressModDataLoader
                    .readAllTags(getBlueprintsFolder(), server.session, Collections.singletonList(REMOVED_BLUEPRINTS_FILENAME))
                    .stream()
                    .map(Blueprint::fromNbt)
                    .forEach(it -> updatedStructures.put(it.filename, it));

            readRemovedBlueprints();
        } else {
            if(tag == null) throw new IllegalStateException("Blueprints folder does not exist and no default data is provided");
            readLegacy(tag);
        }
    }

    private void readRemovedBlueprints() {
        final var removedBlueprintsTag = FortressModDataLoader.readNbt(getRemovedBlueprintsDefaultFileName(), server.session);
        if(removedBlueprintsTag.contains("removedDefaultBlueprints")) {
            final var remBlueprints = removedBlueprintsTag.getString("removedDefaultBlueprints");
            removedDefaultStructures.addAll(Arrays.asList(remBlueprints.split(":")));
        }
    }

    private void readLegacy(NbtCompound tag) {
        if(!tag.contains("updatedStructures")) return;
        updatedStructures.clear();

        Map<String, NbtCompound> structuresMap = new HashMap<>();
        final NbtList nbtElements = tag.getList("updatedStructures", NbtType.COMPOUND);
        for (int i = 0; i < nbtElements.size(); i++) {
            final NbtCompound mapEntry = nbtElements.getCompound(i);
            final String fileName = mapEntry.getString("fileName");
            final NbtCompound structure = mapEntry.getCompound("structure");
            structuresMap.put(fileName, structure);
        }

        Map<String, Integer> floorLevelsMap = new HashMap<>();
        if(tag.contains("floorLevel")) {
            final NbtCompound floorLevel = tag.getCompound("floorLevel");
            for(String key : floorLevel.getKeys()) {
                floorLevelsMap.put(key, floorLevel.getInt(key));
            }
        }

        structuresMap
                .forEach((key, value) -> {
                    final var groupOpt = filenameToGroupConverter.apply(key);
                    final var group = groupOpt.orElseThrow(() -> new IllegalStateException("Can't find group for blueprint " + key));
                    final var bp = new Blueprint(key, floorLevelsMap.getOrDefault(key, 0), value, group);
                    updatedStructures.put(key, bp);
                });
    }

    private record Blueprint(
            String filename,
            int floorLevel,
            NbtCompound tag,
            BlueprintGroup group
    ){
        NbtCompound toNbt() {
            final var nbt = new NbtCompound();
            nbt.putString("filename", filename);
            nbt.put("tag", tag);
            nbt.putInt("floorLevel", floorLevel);
            nbt.putString("group", group.toString());
            return nbt;
        }

        static Blueprint fromNbt(NbtCompound nbt) {
            final var filename = nbt.getString("filename");
            final var tag = nbt.getCompound("tag");
            final var floorLevel = nbt.getInt("floorLevel");
            final var groupStr = nbt.getString("group");
            final var group = BlueprintGroup.valueOf(groupStr);

            return new Blueprint(filename, floorLevel, tag, group);
        }
    }

}