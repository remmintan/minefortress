package org.minefortress.blueprints;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.minefortress.tasks.BlueprintTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ServerBlueprintManager {

    private final Map<String, ServerStructureInfo> structures = new HashMap<>();

    public BlueprintTask createTask(UUID taskId, String structureId, BlockPos startPos, ServerWorld world) {
        if(!structures.containsKey(structureId)) {
            structures.put(structureId, create(structureId, startPos, world));
        }

        final ServerStructureInfo serverStructureInfo = structures.get(structureId);
        final Vec3i size = serverStructureInfo.getSize();
        return new BlueprintTask(taskId, startPos, startPos.add(new Vec3i(size.getX(), size.getY(), size.getZ())), serverStructureInfo.getStructureData());
    }

    private ServerStructureInfo create(String structureId, BlockPos startPos, ServerWorld world) {
        final Optional<Structure> structureOpt = world.getServer().getStructureManager().getStructure(new Identifier(structureId));
        if(structureOpt.isPresent()) {
            final Structure structure = structureOpt.get();

            final Vec3i size = structure.getSize();
            Map<BlockPos, BlockState> structureData = new StructurePlacementData()
                    .getRandomBlockInfos(structure.blockInfoLists, startPos)
                    .getAll()
                    .stream()
                    .filter(inf -> inf.state.getBlock() != Blocks.JIGSAW)
                    .collect(Collectors.toUnmodifiableMap(inf -> inf.pos, inf -> inf.state));

            return new ServerStructureInfo(structureData, size);
        } else {
            throw new IllegalArgumentException("Structure " + structureId + " does not exist");
        }
    }

    private static class ServerStructureInfo {
        private final Map<BlockPos, BlockState> structureData;
        private final Vec3i size;

        public ServerStructureInfo(Map<BlockPos, BlockState> structureData, Vec3i size) {
            this.structureData = structureData;
            this.size = size;
        }

        public Map<BlockPos, BlockState> getStructureData() {
            return structureData;
        }

        public Vec3i getSize() {
            return size;
        }

    }

}
