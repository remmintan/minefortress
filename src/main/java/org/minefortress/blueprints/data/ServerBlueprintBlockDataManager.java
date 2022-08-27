package org.minefortress.blueprints.data;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.Structure;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.minefortress.MineFortressMod;
import org.minefortress.data.FortressModDataLoader;

import java.util.*;
import java.util.stream.Collectors;

public final class ServerBlueprintBlockDataManager extends AbstractBlueprintBlockDataManager{

    private static final String BLUEPRINTS_FOLDER = "blueprints";
    private static final String REMOVED_BLUEPRINTS_FILENAME = "removed_blueprints.nbt";

    private final MinecraftServer server;
    private final Map<String, Blueprint> updatedStructures = new HashMap<>();
    private final Set<String> removedDefaultStructures = new HashSet<>();

    public ServerBlueprintBlockDataManager(MinecraftServer server) {
        this.server = server;
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

    public boolean update(String fileName, NbtCompound tag, int newFloorLevel) {
        final var alreadyIn = updatedStructures.containsKey(fileName);
        updatedStructures.put(fileName, new Blueprint(fileName, newFloorLevel, tag));
        removedDefaultStructures.remove(fileName);
        invalidateBlueprint(fileName);

        final var defaultStructure = getDefaultStructure(fileName).isPresent();
        return alreadyIn || defaultStructure;
    }

    public void remove(String fileName) {
        updatedStructures.remove(fileName);
        if(getDefaultStructure(fileName).isPresent()) {
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
        final Identifier id = new Identifier(MineFortressMod.MOD_ID, blueprintFileName);
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
        FortressModDataLoader.clearFolder(BLUEPRINTS_FOLDER, server.session);
        saveRemovedBlueprints();

        if(updatedStructures.isEmpty()) return;
        final var tags = new HashMap<String, NbtCompound>();
        updatedStructures.forEach((k, v) -> {
            final var tagFileName = BLUEPRINTS_FOLDER + "/" + k + ".nbt";
            final var tag = v.toNbt();
            tags.put(tagFileName, tag);
        });
        FortressModDataLoader.writeAllTags(tags, server.session);
    }

    private void saveRemovedBlueprints() {
        final var removedBlueprintsTag = new NbtCompound();
        final var removedStructs = String.join(":", removedDefaultStructures);
        removedBlueprintsTag.putString("removedDefaultBlueprints", removedStructs);
        FortressModDataLoader.saveNbt(removedBlueprintsTag, getRemovedBlueprintsDefaultFileName(), server.session);
    }

    @NotNull
    private String getRemovedBlueprintsDefaultFileName() {
        return FortressModDataLoader.MOD_DIR + "/" + BLUEPRINTS_FOLDER+"/"+REMOVED_BLUEPRINTS_FILENAME;
    }

    public void readBlockDataManager(NbtCompound tag) {
        if(FortressModDataLoader.exists(BLUEPRINTS_FOLDER, server.session)) {
            updatedStructures.clear();
            removedDefaultStructures.clear();
            FortressModDataLoader
                    .readAllTags(BLUEPRINTS_FOLDER, server.session)
                    .stream()
                    .map(Blueprint::fromNbt)
                    .forEach(it -> updatedStructures.put(it.filename, it));

            readRemovedBlueprints();
        } else {
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
                .entrySet()
                .stream()
                .forEach(it -> {
                    final var key = it.getKey();
                    final var bp = new Blueprint(key, floorLevelsMap.getOrDefault(key, 0), it.getValue());
                    updatedStructures.put(key, bp);
                });
    }

    private static record Blueprint(
            String filename,
            int floorLevel,
            NbtCompound tag
    ){
        NbtCompound toNbt() {
            final var nbt = new NbtCompound();
            nbt.putString("filename", filename);
            nbt.put("tag", tag);
            nbt.putInt("floorLevel", floorLevel);
            return nbt;
        }

        static Blueprint fromNbt(NbtCompound nbt) {
            final var filename = nbt.getString("filename");
            final var tag = nbt.getCompound("tag");
            final var floorLevel = nbt.getInt("floorLevel");

            return new Blueprint(filename, floorLevel, tag);
        }
    }

}