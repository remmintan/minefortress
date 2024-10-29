package org.minefortress.blueprints.data;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintDataLayer;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintsTagsKeeper;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IStructureBlockData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ClientStructureBlockDataProvider extends AbstractStructureBlockDataManager implements BlueprintsTagsKeeper {

    private final Map<String, NbtCompound> blueprintTags = new HashMap<>();

    @Override
    protected Optional<StructureTemplate> getStructure(String blueprintId) {
        if (!blueprintTags.containsKey(blueprintId)) return Optional.empty();
        final NbtCompound blueprintTag = blueprintTags.get(blueprintId);
        final StructureTemplate structure = new StructureTemplate();
        structure.readNbt(Registries.BLOCK.getReadOnlyWrapper(), blueprintTag);
        return Optional.of(structure);
    }

    @Override
    protected IStructureBlockData buildStructure(StructureTemplate structure, BlockRotation rotation, int floorLevel) {
        return buildStructureForClient(structure, rotation);
    }

    public static IStructureBlockData buildStructureForClient(StructureTemplate structure, BlockRotation rotation) {
        final var sizeAndPivot = getSizeAndPivot(structure, rotation);
        final var size = sizeAndPivot.size();
        final var pivot = sizeAndPivot.pivot();

        final Map<BlockPos, BlockState> structureData = getStrcutureData(structure, rotation, pivot);
        return StructureBlockData
                .withBlueprintSize(size)
                .setLayer(BlueprintDataLayer.GENERAL, structureData).build();
    }

    @Override
    public void setBlueprint(String blueprintId, NbtCompound tag) {
        blueprintTags.put(blueprintId, tag);
    }

    @Override
    public void removeBlueprint(String blueprintFileName) {
        blueprintTags.remove(blueprintFileName);
    }

    @Override
    public void reset() {
        super.reset();
        blueprintTags.clear();
    }
}
