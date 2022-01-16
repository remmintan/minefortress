package org.minefortress.blueprints;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.fluid.FluidState;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlueprintChunkRendererRegion extends ChunkRendererRegion {

    private final Map<BlockPos, BlockState> structureData;
    private BlockPos origin = BlockPos.ORIGIN;
    private Vec3i originDelta = BlockPos.ORIGIN;
    private final boolean standsOnGrass;

    public static BlueprintChunkRendererRegion create(Structure structure, World world, BlockPos originPos, BlockRotation rotation) {
        final StructurePlacementData placementData = new StructurePlacementData().setRotation(rotation);
        final List<Structure.StructureBlockInfo> blockInfos = placementData
                .getRandomBlockInfos(structure.blockInfoLists, BlockPos.ORIGIN)
                .getAll();


        final Vec3i structureSize = structure.getRotatedSize(rotation);
        final BlockPos startPos = BlockPos.ORIGIN;

        final int biggerStructureSide = Math.max(structureSize.getX(), structureSize.getZ());
        final Vec3i delta = new Vec3i(biggerStructureSide / 2, 0, biggerStructureSide / 2);
        final BlockPos pivot = startPos.add(delta);
        placementData.setPosition(pivot);
        final BlockPos endPos = startPos.add(structureSize);

        final Map<BlockPos, BlockState> structureData = blockInfos
                .stream()
                .filter(info -> info.state.getBlock() != Blocks.AIR)
                .map(BlueprintMetadataManager::convertJigsawBlock)
                .collect(Collectors.toMap(
                        inf -> Structure.transform(placementData, inf.pos.add(originPos)),
                        inf -> inf.state.rotate(rotation)
                ));

        final boolean standsOnGrass = structureData.entrySet().stream().filter(entry -> entry.getKey().getY() == 0).allMatch(entry -> {
            final Block block = entry.getValue().getBlock();
            return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK;
        });

        final WorldChunk[][] worldChunks = new WorldChunk[1][1];
        worldChunks[0][0] = world.getChunk(0,0);
        return new BlueprintChunkRendererRegion(world, 0, 0, worldChunks, startPos, endPos, structureData, standsOnGrass);
    }

    public BlueprintChunkRendererRegion(World world, int chunkX, int chunkZ, WorldChunk[][] chunks, BlockPos startPos, BlockPos endPos, Map<BlockPos, BlockState> structureData, boolean standsOnGrass) {
        super(world, chunkX, chunkZ, chunks, BlockPos.ORIGIN, BlockPos.ORIGIN);
        this.standsOnGrass = standsOnGrass;
        this.sizeX = endPos.getX() - startPos.getX() + 1;
        this.sizeY = endPos.getY() - startPos.getY() + 1;
        this.sizeZ = endPos.getZ() - startPos.getZ() + 1;
        this.blockStates = new BlockState[this.sizeX * this.sizeY * this.sizeZ];
        this.structureData = structureData;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        pos = adjustPos(pos);
        return structureData.getOrDefault(pos, Blocks.AIR.getDefaultState());
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        pos = adjustPos(pos);
        return this.getBlockState(pos).getFluidState();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        final BlockState blockState = this.getBlockState(pos);
        if (blockState.getBlock() instanceof BlockEntityProvider provider) {
            return provider.createBlockEntity(pos.subtract(originDelta), blockState);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos, WorldChunk.CreationType creationType) {
        return this.getBlockEntity(pos);
    }

    public boolean isStandsOnGround() {
        return standsOnGrass;
    }

    public void setOrigin(BlockPos origin, Vec3i originDelta) {
        this.origin = origin;
        this.originDelta = originDelta;
    }

    private BlockPos adjustPos(BlockPos pos) {
        return pos.subtract(origin);
    }

    public Map<BlockPos, BlockState> getStructureData() {
        return structureData;
    }
}
