package org.minefortress.blueprints.data;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.Structure;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.minefortress.blueprints.interfaces.BlueprintsTagsKeeper;

import java.util.HashMap;
import java.util.Map;

public final class ClientBlueprintBlockDataManager extends AbstractBlueprintBlockDataManager implements BlueprintsTagsKeeper {

    private final Map<String, NbtCompound> blueprintTags = new HashMap<>();

    @Override
    protected Structure getStructure(String blueprintFileName) {
        if(!blueprintTags.containsKey(blueprintFileName)) throw new IllegalArgumentException("Blueprint file not found: " + blueprintFileName);
        final NbtCompound blueprintTag = blueprintTags.get(blueprintFileName);
        final Structure structure = new Structure();
        structure.readNbt(blueprintTag);
        return structure;
    }

    @Override
    protected BlueprintBlockData buildBlueprint(Structure structure, BlockRotation rotation) {
        Vec3i size = structure.getRotatedSize(rotation);
        final int biggerSide = Math.max(size.getX(), size.getZ());
        final BlockPos pivot = BlockPos.ORIGIN.add(biggerSide / 2, 0, biggerSide / 2);

        size = new Vec3i(biggerSide, size.getY(), biggerSide);

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
