package org.minefortress.blueprints.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.nbt.NbtElement;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AbstractBlueprintBlockDataManager {

    private final Map<String, BlueprintBlockData> blueprints = new HashMap<>();

    public BlueprintBlockData getBlockData(String blueprintFileName, BlockRotation rotation) {
        return getBlockData(blueprintFileName, rotation, 0);
    }

    public BlueprintBlockData getBlockData(String blueprintFileName, BlockRotation rotation, int floorLevel) {
        final String key = getKey(blueprintFileName, rotation);
        if(!blueprints.containsKey(key)) {
            final Structure structure = getStructure(blueprintFileName)
                    .orElseThrow(() -> new IllegalStateException("Blueprint not found " + blueprintFileName));
            final BlueprintBlockData blueprintBlockData = buildBlueprint(structure, rotation, floorLevel);
            blueprints.put(key, blueprintBlockData);
        }

        return blueprints.get(key);
    }

    public void invalidateBlueprint(String fileName) {
        new HashSet<>(blueprints.keySet()).stream().filter(key -> key.startsWith(fileName)).forEach(blueprints::remove);
    }

    protected abstract Optional<Structure> getStructure(String blueprintFileName);
    protected abstract BlueprintBlockData buildBlueprint(Structure structure, BlockRotation rotation, int floorLevel);

    @NotNull
    protected Map<BlockPos, BlockState> getStrcutureData(Structure structure, BlockRotation rotation, BlockPos pivot) {
        final StructurePlacementData placementData = new StructurePlacementData().setRotation(rotation);
        final List<Structure.StructureBlockInfo> blockInfos = placementData
                .getRandomBlockInfos(structure.blockInfoLists, pivot)
                .getAll();
        placementData.setPosition(pivot);

        final var convertedStructureBlocks = blockInfos
                .stream()
                .map(AbstractBlueprintBlockDataManager::convertJigsawBlock)
                .toList();

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;

        for(var structureBlock : convertedStructureBlocks) {
            final var transformedPos = Structure.transform(placementData, structureBlock.pos);
            if(transformedPos.getX() < minX) {
                minX = transformedPos.getX();
            }
            if(transformedPos.getY() < minY) {
                minY = transformedPos.getY();
            }
            if(transformedPos.getZ() < minZ) {
                minZ = transformedPos.getZ();
            }
        }
        final var minPos = new BlockPos(minX, minY, minZ);

        return convertedStructureBlocks
                .stream()
                .collect(Collectors.toMap(
                        inf -> Structure.transform(placementData, inf.pos).subtract(minPos),
                        inf -> inf.state.rotate(rotation)
                ));
    }

    @NotNull
    private String getKey(String fileName, BlockRotation rotation) {
        return fileName + ":" + rotation.name();
    }

    protected static Structure.StructureBlockInfo convertJigsawBlock(Structure.StructureBlockInfo inf) {
        if(inf.state.isOf(Blocks.JIGSAW)) {
            final NbtElement final_state = inf.nbt.get("final_state");
            if(final_state != null) {
                final String stateString = final_state.asString();
                BlockState blockState = null;
                try {
                    blockState = new BlockArgumentParser(new StringReader(stateString), false)
                            .parse(false)
                            .getBlockState();
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }

                if(blockState != null)
                    return new Structure.StructureBlockInfo(inf.pos, blockState, inf.nbt);
            }
        }
        return inf;
    }

    protected void reset() {
        this.blueprints.clear();
    }

    protected SizeAndPivot getSizeAndPivot(Structure structure, BlockRotation rotation) {
        Vec3i size = structure.getRotatedSize(rotation);
//        final int biggerSide = Math.max(size.getX(), size.getZ());
        final BlockPos pivot = BlockPos.ORIGIN.add(size.getX() / 2, 0,  size.getZ() / 2);
        size = new Vec3i(size.getX(), size.getY(), size.getZ());
        return new SizeAndPivot(size, pivot);
    }

    protected record SizeAndPivot(Vec3i size, BlockPos pivot) {}
}
