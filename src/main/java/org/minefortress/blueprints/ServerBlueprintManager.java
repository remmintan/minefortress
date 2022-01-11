package org.minefortress.blueprints;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.minefortress.tasks.BlueprintTask;

import java.util.*;
import java.util.stream.Collectors;

public class ServerBlueprintManager {

    private final Map<String, ServerStructureInfo> structures = new HashMap<>();

    public BlueprintTask createTask(UUID taskId, String structureId, String structureFile, BlockPos startPos, ServerWorld world, BlockRotation rotation) {
        if(!structures.containsKey(structureId)) {
            structures.put(structureId, create(structureFile, startPos, world, rotation));
        }

        final ServerStructureInfo serverStructureInfo = structures.get(structureId);
        final Vec3i size = serverStructureInfo.getSize();
        return new BlueprintTask(taskId, startPos, startPos.add(new Vec3i(size.getX(), size.getY(), size.getZ())), serverStructureInfo.getStructureData(), serverStructureInfo.getStructureEntityData());
    }

    private ServerStructureInfo create(String structureId, BlockPos startPos, ServerWorld world, BlockRotation rotation) {
        final Optional<Structure> structureOpt = world.getServer().getStructureManager().getStructure(new Identifier(structureId));
        if(structureOpt.isPresent()) {
            final Structure structure = structureOpt.get();

            final Vec3i size = structure.getRotatedSize(rotation);
            final double biggerSideSize = Math.max(size.getX(), size.getZ());
            final Vec3i delta = new Vec3i(biggerSideSize / 2, 0, biggerSideSize / 2);
            final BlockPos pivot = BlockPos.ORIGIN.add(delta);

            final StructurePlacementData structurePlacementData = new StructurePlacementData()
                    .setRotation(rotation);
            final List<Structure.StructureBlockInfo> allBlockInfos = structurePlacementData
                    .getRandomBlockInfos(structure.blockInfoLists, startPos)
                    .getAll();

            structurePlacementData.setPosition(pivot);

            Map<BlockPos, BlockState> totalStructureData = allBlockInfos
                    .stream()
                    .filter(inf -> inf.state.getBlock() != Blocks.JIGSAW)
                    .collect(Collectors.toUnmodifiableMap(
                            inf -> Structure.transform(structurePlacementData, inf.pos),
                            inf -> inf.state.rotate(rotation)
                    ));

            final Map<BlockPos, BlockState> structureData = totalStructureData.entrySet()
                    .stream()
                    .filter(entry -> !(entry.getValue().getBlock() instanceof BlockEntityProvider))
                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

            final Map<BlockPos, BlockState> structureEntityData = totalStructureData.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().getBlock() instanceof BlockEntityProvider)
                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

            return new ServerStructureInfo(structureData, structureEntityData, size);
        } else {
            throw new IllegalArgumentException("Structure " + structureId + " does not exist");
        }
    }

    private static class ServerStructureInfo {
        private final Map<BlockPos, BlockState> structureData;
        private final Map<BlockPos, BlockState> structureEntityData;
        private final Vec3i size;

        public ServerStructureInfo(Map<BlockPos, BlockState> structureData, Map<BlockPos, BlockState> structureEntityData, Vec3i size) {
            this.structureData = structureData;
            this.structureEntityData = structureEntityData;
            this.size = size;
        }

        public Map<BlockPos, BlockState> getStructureData() {
            return structureData;
        }

        public Map<BlockPos, BlockState> getStructureEntityData() {
            return structureEntityData;
        }

        public Vec3i getSize() {
            return size;
        }
    }

}
