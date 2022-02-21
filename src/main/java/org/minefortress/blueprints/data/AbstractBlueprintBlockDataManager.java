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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.minefortress.MineFortressMod;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract class AbstractBlueprintBlockDataManager {

    private Map<String, BlueprintBlockData> blueprints;

    public BlueprintBlockData getBlockData(String blueprintFileName, BlockRotation rotation) {
        final String key = getKey(blueprintFileName, rotation);
        if(!blueprints.containsKey(key)) {
            final Structure structure = getStructure(blueprintFileName);
            final BlueprintBlockData blueprintBlockData = buildBlueprint(structure, rotation);
            blueprints.put(key, blueprintBlockData);
        }

        return blueprints.get(key);
    }

    public void invalidateBlueprint(String fileName) {
        new HashSet<>(blueprints.keySet()).stream().filter(key -> key.startsWith(fileName)).forEach(blueprints::remove);
    }

    protected abstract Structure getStructure(String blueprintFileName);
    protected abstract BlueprintBlockData buildBlueprint(Structure structure, BlockRotation rotation);

    @NotNull
    protected Map<BlockPos, BlockState> getStrcutureData(Structure structure, BlockRotation rotation, BlockPos pivot) {
        final StructurePlacementData placementData = new StructurePlacementData().setRotation(rotation);
        final List<Structure.StructureBlockInfo> blockInfos = placementData
                .getRandomBlockInfos(structure.blockInfoLists, BlockPos.ORIGIN)
                .getAll();
        placementData.setPosition(pivot);

        return blockInfos
                .stream()
                .filter(info -> info.state.getBlock() != Blocks.AIR)
                .map(AbstractBlueprintBlockDataManager::convertJigsawBlock)
                .collect(Collectors.toMap(
                        inf -> Structure.transform(placementData, inf.pos.add(BlockPos.ORIGIN)),
                        inf -> inf.state.rotate(rotation)
                ));
    }

    protected boolean isStandsOnGrass(Map<BlockPos, BlockState> structureData) {
        return structureData.entrySet().stream().filter(entry -> entry.getKey().getY() == 0).allMatch(entry -> {
            final Block block = entry.getValue().getBlock();
            return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK;
        });
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
}