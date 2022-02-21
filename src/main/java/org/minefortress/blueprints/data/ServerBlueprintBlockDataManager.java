package org.minefortress.blueprints.data;

import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.Structure;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.minefortress.MineFortressMod;

import java.util.Map;

public final class ServerBlueprintBlockDataManager extends AbstractBlueprintBlockDataManager{

    private final MinecraftServer server;

    public ServerBlueprintBlockDataManager(MinecraftServer server) {
        this.server = server;
    }

    @Override
    protected Structure getStructure(String blueprintFileName) {
        final Identifier id = getId(blueprintFileName);
        return server
                .getStructureManager()
                .getStructure(id)
                .orElseThrow(() -> new IllegalArgumentException("Blueprint file not found: " + blueprintFileName));
    }

    @Override
    protected BlueprintBlockData buildBlueprint(Structure structure, BlockRotation rotation) {
        Vec3i size = structure.getRotatedSize(rotation);
        final int biggerSide = Math.max(size.getX(), size.getZ());
        final BlockPos pivot = BlockPos.ORIGIN.add(biggerSide / 2, 0, biggerSide / 2);

        size = new Vec3i(biggerSide, size.getY(), biggerSide);

        final Map<BlockPos, BlockState> structureData = getStrcutureData(structure, rotation, pivot);
        final boolean standsOnGrass = isStandsOnGrass(structureData);

        return BlueprintBlockData
                .withBlueprintSize(size)
                .setStandsOnGrass(standsOnGrass)
                .setLayer(BlueprintDataLayer.GENERAL, structureData)
                .setLayer(BlueprintDataLayer.MANUAL, structureData)
                .build();
    }

    @NotNull
    private Identifier getId(String fileName) {
        return new Identifier(MineFortressMod.MOD_ID, fileName);
    }

}


//    final BlueprintMetadata blueprintMetadata = BlueprintMetadataManager.getByFile(fileName);
//
//    final Map<BlockPos, BlockState> structureEntityData = structureData.entrySet()
//            .stream()
//            .filter(entry -> entry.getValue().getBlock() instanceof BlockEntityProvider)
//            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
//
//    final Map<BlockPos, BlockState> structureManualData = structureData.entrySet()
//            .stream()
//            .filter(entry -> !(entry.getValue().getBlock() instanceof BlockEntityProvider))
//            .filter(ent -> blueprintMetadata == null || !blueprintMetadata.isPartOfAutomaticLayer(ent.getKey(), ent.getValue()))
//            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
//
//    final Map<BlockPos, BlockState> structureAutomaticData = structureData.entrySet()
//            .stream()
//            .filter(entry -> !(entry.getValue().getBlock() instanceof BlockEntityProvider))
//            .filter(ent -> blueprintMetadata != null && blueprintMetadata.isPartOfAutomaticLayer(ent.getKey(), ent.getValue()))
//            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
//
//    BlueprintBlockData blockData = new BlueprintBlockData(structureData, size, standsOnGrass, structureEntityData, structureManualData, structureAutomaticData);
//                blueprints.put(key, blockData);