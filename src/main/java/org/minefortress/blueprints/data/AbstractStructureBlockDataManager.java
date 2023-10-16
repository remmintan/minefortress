package org.minefortress.blueprints.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IStructureBlockData;
import org.jetbrains.annotations.NotNull;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlockDataProvider;

import java.util.*;
import java.util.stream.Collectors;

abstract class AbstractStructureBlockDataManager implements IBlockDataProvider {

    private final Map<String, IStructureBlockData> blueprints = new HashMap<>();

    public IStructureBlockData getBlockData(String blueprintId, BlockRotation rotation) {
        return getBlockData(blueprintId, rotation, 0);
    }

    public IStructureBlockData getBlockData(String blueprintId, BlockRotation rotation, int floorLevel) {
        final String key = getKey(blueprintId, rotation);
        if(!blueprints.containsKey(key)) {
            final StructureTemplate structure = getStructure(blueprintId)
                    .orElseThrow(() -> new IllegalStateException("Blueprint not found " + blueprintId));
            final IStructureBlockData blueprintBlockData = buildStructure(structure, rotation, floorLevel);
            blueprints.put(key, blueprintBlockData);
        }

        return blueprints.get(key);
    }

    public void invalidateBlueprint(String fileName) {
        new HashSet<>(blueprints.keySet()).stream().filter(key -> key.startsWith(fileName)).forEach(blueprints::remove);
    }

    protected abstract Optional<StructureTemplate> getStructure(String blueprintFileName);
    protected abstract IStructureBlockData buildStructure(StructureTemplate structure, BlockRotation rotation, int floorLevel);

    @NotNull
    protected static Map<BlockPos, BlockState> getStrcutureData(StructureTemplate structure, BlockRotation rotation, BlockPos pivot) {
        final StructurePlacementData placementData = new StructurePlacementData().setRotation(rotation);
        final List<StructureTemplate.StructureBlockInfo> blockInfos = placementData
                .getRandomBlockInfos(structure.blockInfoLists, pivot)
                .getAll();
        placementData.setPosition(pivot);

        final var convertedStructureBlocks = blockInfos
                .stream()
                .map(AbstractStructureBlockDataManager::convertJigsawBlock)
                .toList();

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;

        for(var structureBlock : convertedStructureBlocks) {
            final var transformedPos = StructureTemplate.transform(placementData, structureBlock.pos);
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
                        inf -> StructureTemplate.transform(placementData, inf.pos).subtract(minPos),
                        inf -> inf.state.rotate(rotation)
                ));
    }

    @NotNull
    private static String getKey(String id, BlockRotation rotation) {
        return id + ":" + rotation.name();
    }

    protected static StructureTemplate.StructureBlockInfo convertJigsawBlock(StructureTemplate.StructureBlockInfo inf) {
        if(inf.state.isOf(Blocks.JIGSAW)) {
            final NbtElement final_state = inf.nbt.get("final_state");
            if(final_state != null) {
                final String stateString = final_state.asString();
                BlockState blockState = null;
                try {
                    blockState = BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), new StringReader(stateString), false).blockState();
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }

                if(blockState != null)
                    return new StructureTemplate.StructureBlockInfo(inf.pos, blockState, inf.nbt);
            }
        }
        return inf;
    }

    public void reset() {
        this.blueprints.clear();
    }

    protected static SizeAndPivot getSizeAndPivot(StructureTemplate structure, BlockRotation rotation) {
        Vec3i size = structure.getRotatedSize(rotation);
        final BlockPos pivot = BlockPos.ORIGIN.add(size.getX() / 2, 0,  size.getZ() / 2);
        size = new Vec3i(size.getX(), size.getY(), size.getZ());
        return new SizeAndPivot(size, pivot);
    }

    protected record SizeAndPivot(Vec3i size, BlockPos pivot) {}
}
