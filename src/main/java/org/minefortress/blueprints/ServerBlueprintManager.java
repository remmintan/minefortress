package org.minefortress.blueprints;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.minefortress.tasks.BlueprintTask;

import java.util.UUID;

public class ServerBlueprintManager {

    private final BlueprintBlockDataManager blockDataManager;

//    private final Map<String, ServerStructureInfo> structures = new HashMap<>();

    public ServerBlueprintManager(final MinecraftServer server) {
        this.blockDataManager = new BlueprintBlockDataManager(() -> server);
    }

    public BlueprintTask createTask(UUID taskId, String structureId, String structureFile, BlockPos startPos, ServerWorld world, BlockRotation rotation) {
//        if(!structures.containsKey(structureId)) {
//            structures.put(structureId, create(structureFile, startPos, world, rotation));
//        }

//        final ServerStructureInfo serverStructureInfo = structures.get(structureId);

        final BlueprintBlockDataManager.BlueprintBlockData serverStructureInfo = blockDataManager.getBlockData(structureFile, rotation, true);
        final Vec3i size = serverStructureInfo.getSize();
        return new BlueprintTask(taskId, startPos, startPos.add(new Vec3i(size.getX(), size.getY(), size.getZ())), serverStructureInfo.getManualLayer(), serverStructureInfo.getEntityLayer(), serverStructureInfo.getAutomaticLayer());
    }

//    private ServerStructureInfo create(String structureId, BlockPos startPos, ServerWorld world, BlockRotation rotation) {
//        final Optional<Structure> structureOpt = world.getServer().getStructureManager().getStructure(new Identifier(structureId));
//        if(structureOpt.isPresent()) {
//            final Structure structure = structureOpt.get();
//
//            final Vec3i size = structure.getRotatedSize(rotation);
//            final double biggerSideSize = Math.max(size.getX(), size.getZ());
//            final Vec3i delta = new Vec3i(biggerSideSize / 2, 0, biggerSideSize / 2);
//            final BlockPos pivot = BlockPos.ORIGIN.add(delta);
//
//            final StructurePlacementData structurePlacementData = new StructurePlacementData()
//                    .setRotation(rotation);
//            final List<Structure.StructureBlockInfo> allBlockInfos = structurePlacementData
//                    .getRandomBlockInfos(structure.blockInfoLists, startPos)
//                    .getAll();
//
//            structurePlacementData.setPosition(pivot);
//
//            Map<BlockPos, BlockState> totalStructureData = allBlockInfos
//                    .stream()
//                    .map(BlueprintMetadataManager::convertJigsawBlock)
//                    .collect(
//                        Collectors.toUnmodifiableMap(
//                            inf -> Structure.transform(structurePlacementData, inf.pos).toImmutable(),
//                            inf -> inf.state.rotate(rotation)
//                        )
//                    );
//
//            final BlueprintMetadata blueprintMetadata = BlueprintMetadataManager.getByFile(structureId);
//
//
//            final List<Map.Entry<BlockPos, BlockState>> structureData = totalStructureData.entrySet()
//                    .stream()
//                    .filter(entry -> !(entry.getValue().getBlock() instanceof BlockEntityProvider))
//                    .collect(Collectors.toList());
//
//            final Map<BlockPos, BlockState> structureEntityData = totalStructureData.entrySet()
//                    .stream()
//                    .filter(entry -> entry.getValue().getBlock() instanceof BlockEntityProvider)
//                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
//
//            final Map<BlockPos, BlockState> structureManualData = structureData
//                    .stream()
//                    .filter(ent -> blueprintMetadata == null || !blueprintMetadata.isPartOfAutomaticLayer(ent.getKey(), ent.getValue()))
//                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
//
//            final Map<BlockPos, BlockState> structureAutomaticData = structureData
//                    .stream()
//                    .filter(ent -> blueprintMetadata != null && blueprintMetadata.isPartOfAutomaticLayer(ent.getKey(), ent.getValue()))
//                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
//
//            return new ServerStructureInfo(structureManualData, structureEntityData, structureAutomaticData, size);
//        } else {
//            throw new IllegalArgumentException("Structure " + structureId + " does not exist");
//        }
//    }

//    private static class ServerStructureInfo {
//        private final Map<BlockPos, BlockState> structureData;
//        private final Map<BlockPos, BlockState> structureEntityData;
//        private final Map<BlockPos, BlockState> structureAutomaticBlocks;
//        private final Vec3i size;
//
//        public ServerStructureInfo(Map<BlockPos, BlockState> structureData, Map<BlockPos, BlockState> structureEntityData, Map<BlockPos, BlockState> structureAutomaticBlocks, Vec3i size) {
//            this.structureData = structureData;
//            this.structureEntityData = structureEntityData;
//            this.structureAutomaticBlocks = structureAutomaticBlocks;
//            this.size = size;
//        }
//
//        public Map<BlockPos, BlockState> getStructureData() {
//            return structureData;
//        }
//
//        public Map<BlockPos, BlockState> getStructureEntityData() {
//            return structureEntityData;
//        }
//
//        public Map<BlockPos, BlockState> getStructureAutomaticBlocks() {
//            return structureAutomaticBlocks;
//        }
//
//        public Vec3i getSize() {
//            return size;
//        }
//    }

}
