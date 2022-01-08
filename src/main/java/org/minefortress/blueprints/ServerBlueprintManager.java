package org.minefortress.blueprints;

import net.minecraft.block.BlockState;
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
        return new BlueprintTask(taskId, serverStructureInfo.getStartPos(), serverStructureInfo.getEndPos(), serverStructureInfo.getStructureData());
    }

    private ServerStructureInfo create(String structureId, BlockPos startPos, ServerWorld world) {
        final Optional<Structure> structureOpt = world.getServer().getStructureManager().getStructure(new Identifier(structureId));
        if(structureOpt.isPresent()) {
            final Structure structure = structureOpt.get();

            final Vec3i size = structure.getSize();
            BlockPos endPos = startPos.add(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
            Map<BlockPos, BlockState> structureData = new StructurePlacementData()
                    .getRandomBlockInfos(structure.blockInfoLists, startPos)
                    .getAll()
                    .stream()
                    .collect(Collectors.toUnmodifiableMap(inf -> inf.pos, inf -> inf.state));

            return new ServerStructureInfo(structureData, startPos, endPos);
        } else {
            throw new IllegalArgumentException("Structure " + structureId + " does not exist");
        }
    }

    private static class ServerStructureInfo {
        private final Map<BlockPos, BlockState> structureData;
        private final BlockPos startPos;
        private final BlockPos endPos;

        public ServerStructureInfo(Map<BlockPos, BlockState> structureData, BlockPos startPos, BlockPos endPos) {
            this.structureData = structureData;
            this.startPos = startPos;
            this.endPos = endPos;
        }

        public Map<BlockPos, BlockState> getStructureData() {
            return structureData;
        }

        public BlockPos getStartPos() {
            return startPos;
        }

        public BlockPos getEndPos() {
            return endPos;
        }
    }

}
