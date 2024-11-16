package org.minefortress.blueprints.data;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintDataLayer;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IServerStructureBlockDataManager;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IStructureBlockData;
import org.minefortress.MineFortressMod;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ServerStructureBlockDataManager extends AbstractStructureBlockDataManager implements IServerStructureBlockDataManager {

    private final MinecraftServer server;

    public ServerStructureBlockDataManager(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public Optional<NbtCompound> getStructureNbt(String blueprintId) {
        return getStructure(blueprintId).map(it -> {
            NbtCompound compound = new NbtCompound();
            it.writeNbt(compound);
            return compound;
        });
    }

    @Override
    public void addOrUpdate(String blueprintId, NbtCompound tag) {
        final var id = Identifier.of("minefortress", blueprintId);
        final var structuresManager = server.getStructureTemplateManager();

        final var structure = structuresManager.getTemplateOrBlank(id);
        structure.readNbt(Registries.BLOCK.getReadOnlyWrapper(), tag);
        structuresManager.saveTemplate(id);
        super.invalidateBlueprint(blueprintId);
    }

    @Override
    public void remove(String blueprintId) {
        final var id = Identifier.of("minefortress", blueprintId);
        server.getStructureTemplateManager().unloadTemplate(id);
        super.invalidateBlueprint(blueprintId);
    }

    @Override
    protected Optional<StructureTemplate> getStructure(String blueprintId) {
        final Identifier id = new Identifier(MineFortressMod.MOD_ID, blueprintId);
        return server.getStructureTemplateManager().getTemplate(id);
    }

    @Override
    protected IStructureBlockData buildStructure(StructureTemplate structure, BlockRotation rotation, int floorLevel) {
        final var sizeAndPivot = getSizeAndPivot(structure, rotation);
        final var size = sizeAndPivot.size();
        final var pivot = sizeAndPivot.pivot();

        final Map<BlockPos, BlockState> structureData = getStrcutureData(structure, rotation, pivot);

        final Map<BlockPos, BlockState> structureEntityData = structureData.entrySet()
            .stream()
            .filter(entry -> entry.getValue().getBlock() instanceof BlockEntityProvider || !entry.getValue().getFluidState().isEmpty() || entry.getValue().isIn(BlockTags.TRAPDOORS))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        final Map<BlockPos, BlockState> allBlocksWithoutEntities = structureData.entrySet()
            .stream()
            .filter(entry -> {
                final var state = entry.getValue();
                return !(state.getBlock() instanceof BlockEntityProvider) && state.getFluidState().isEmpty() && !state.isIn(BlockTags.TRAPDOORS);
            })
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

        return StructureBlockData
                .withBlueprintSize(size)
                .setLayer(BlueprintDataLayer.GENERAL, structureData)
                .setLayer(BlueprintDataLayer.MANUAL, manualData)
                .setLayer(BlueprintDataLayer.AUTOMATIC, automaticData)
                .setLayer(BlueprintDataLayer.ENTITY, structureEntityData)
                .build();
    }
}