package org.minefortress.blueprints;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BlueprintBlockDataManager {

    private final Supplier<MinecraftServer> serverSupplier;
    private final Map<String, BlueprintBlockData> blueprints = new HashMap<>();

    public BlueprintBlockDataManager(Supplier<MinecraftServer> serverSupplier) {
        this.serverSupplier = serverSupplier;
    }

    public BlueprintBlockData getBlockData(String fileName, BlockRotation rotation, boolean separateLayers) {
        String key = fileName + ":" + rotation.name() + ":" + separateLayers;
        if (!blueprints.containsKey(key)) {
            final Structure structure = serverSupplier
                    .get()
                    .getStructureManager()
                    .getStructure(new Identifier(fileName))
                    .orElseThrow(() -> new IllegalArgumentException("Blueprint file not found: " + fileName));

            final StructurePlacementData placementData = new StructurePlacementData().setRotation(rotation);
            final List<Structure.StructureBlockInfo> blockInfos = placementData
                    .getRandomBlockInfos(structure.blockInfoLists, BlockPos.ORIGIN)
                    .getAll();

            final Vec3i size = structure.getRotatedSize(rotation);
            final BlockPos origin = BlockPos.ORIGIN;
            final int biggerSide = Math.max(size.getX(), size.getZ());
            final BlockPos pivot = origin.add(biggerSide / 2, 0, biggerSide / 2);

            placementData.setPosition(pivot);

            final Map<BlockPos, BlockState> structureData = blockInfos
                    .stream()
                    .filter(info -> info.state.getBlock() != Blocks.AIR)
                    .map(BlueprintMetadataManager::convertJigsawBlock)
                    .collect(Collectors.toMap(
                            inf -> Structure.transform(placementData, inf.pos.add(origin)),
                            inf -> inf.state.rotate(rotation)
                    ));

            final boolean standsOnGrass = structureData.entrySet().stream().filter(entry -> entry.getKey().getY() == 0).allMatch(entry -> {
                final Block block = entry.getValue().getBlock();
                return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK;
            });

            if(separateLayers) {
                final BlueprintMetadata blueprintMetadata = BlueprintMetadataManager.getByFile(fileName);


                final Map<BlockPos, BlockState> structureEntityData = structureData.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().getBlock() instanceof BlockEntityProvider)
                        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

                final Map<BlockPos, BlockState> structureManualData = structureData.entrySet()
                        .stream()
                        .filter(ent -> blueprintMetadata == null || !blueprintMetadata.isPartOfAutomaticLayer(ent.getKey(), ent.getValue()))
                        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

                final Map<BlockPos, BlockState> structureAutomaticData = structureData.entrySet()
                        .stream()
                        .filter(ent -> blueprintMetadata != null && blueprintMetadata.isPartOfAutomaticLayer(ent.getKey(), ent.getValue()))
                        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

                BlueprintBlockData blockData = new BlueprintBlockData(structureData, size, standsOnGrass, structureEntityData, structureManualData, structureAutomaticData);
                blueprints.put(key, blockData);
            } else {
                BlueprintBlockData blockData = new BlueprintBlockData(structureData, size, standsOnGrass, null, null, null);
                blueprints.put(key, blockData);
            }
        }

        return blueprints.get(key);
    }

    public static class BlueprintBlockData {
        private final Map<BlockPos, BlockState> blueprintData;
        private final Vec3i size;
        private final boolean standsOnGrass;

        private final Map<BlockPos, BlockState> entityLayer;
        private final Map<BlockPos, BlockState> manualLayer;
        private final Map<BlockPos, BlockState> automaticLayer;

        public BlueprintBlockData(Map<BlockPos, BlockState> blueprintData, Vec3i size, boolean standsOnGrass, Map<BlockPos, BlockState> entityLayer, Map<BlockPos, BlockState> manualLayer, Map<BlockPos, BlockState> automaticLayer) {
            this.blueprintData = blueprintData;
            this.size = size;
            this.standsOnGrass = standsOnGrass;
            this.entityLayer = entityLayer;
            this.manualLayer = manualLayer;
            this.automaticLayer = automaticLayer;
        }

        public Map<BlockPos, BlockState> getBlueprintData() {
            return blueprintData;
        }

        public Vec3i getSize() {
            return size;
        }

        public boolean isStandsOnGrass() {
            return standsOnGrass;
        }

        public Map<BlockPos, BlockState> getEntityLayer() {
            return entityLayer;
        }

        public Map<BlockPos, BlockState> getManualLayer() {
            return manualLayer;
        }

        public Map<BlockPos, BlockState> getAutomaticLayer() {
            return automaticLayer;
        }
    }

}
