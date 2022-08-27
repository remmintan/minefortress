package org.minefortress.blueprints.data;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.Structure;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import org.minefortress.blueprints.interfaces.BlueprintsTagsKeeper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ClientBlueprintBlockDataManager extends AbstractBlueprintBlockDataManager implements BlueprintsTagsKeeper {

    private final Map<String, NbtCompound> blueprintTags = new HashMap<>();

    @Override
    protected Optional<Structure> getStructure(String blueprintFileName) {
        if(!blueprintTags.containsKey(blueprintFileName)) return Optional.empty();
        final NbtCompound blueprintTag = blueprintTags.get(blueprintFileName);
        final Structure structure = new Structure();
        structure.readNbt(blueprintTag);
        return Optional.of(structure);
    }

    @Override
    protected BlueprintBlockData buildBlueprint(Structure structure, BlockRotation rotation, int floorLevel) {
        final var sizeAndPivot = getSizeAndPivot(structure, rotation);
        final var size = sizeAndPivot.size();
        final var pivot = sizeAndPivot.pivot();

        final Map<BlockPos, BlockState> structureData = getStrcutureData(structure, rotation, pivot);
        return BlueprintBlockData
                .withBlueprintSize(size)
                .setLayer(BlueprintDataLayer.GENERAL, structureData).build();
    }

    @Override
    public void setBlueprint(String blueprintFileName, NbtCompound tag) {
        blueprintTags.put(blueprintFileName, tag);
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
