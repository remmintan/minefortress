package org.minefortress.blueprints;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
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

    public static BlueprintChunkRendererRegion create(Structure structure, World world, BlockPos originPos) {
        final StructurePlacementData placementData = new StructurePlacementData();
        final List<Structure.StructureBlockInfo> blockInfos = placementData
                .getRandomBlockInfos(structure.blockInfoLists, BlockPos.ORIGIN)
                .getAll();
        final Map<BlockPos, BlockState> structureData = blockInfos
                .stream()
                .collect(Collectors.toMap(inf -> inf.pos.add(originPos), inf -> inf.state));


        final Vec3i structureSize = structure.getSize();
        final BlockPos startPos = BlockPos.ORIGIN;
        final BlockPos endPos = startPos.add(structureSize);

        final WorldChunk[][] worldChunks = new WorldChunk[1][1];
        worldChunks[0][0] = world.getChunk(0,0);
        return new BlueprintChunkRendererRegion(world, 0, 0, worldChunks, startPos, endPos, structureData);
    }

    public BlueprintChunkRendererRegion(World world, int chunkX, int chunkZ, WorldChunk[][] chunks, BlockPos startPos, BlockPos endPos, Map<BlockPos, BlockState> structureData) {
        super(world, chunkX, chunkZ, chunks, BlockPos.ORIGIN, BlockPos.ORIGIN);
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
        pos = adjustPos(pos);
        final BlockState blockState = this.getBlockState(pos);
        if (blockState.getBlock() instanceof BlockEntityProvider provider) {
            return provider.createBlockEntity(pos, blockState);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos, WorldChunk.CreationType creationType) {
        pos = adjustPos(pos);
        return this.getBlockEntity(pos);
    }

    public void setOrigin(BlockPos origin) {
        this.origin = origin;
    }

    private BlockPos adjustPos(BlockPos pos) {
        return pos.subtract(origin);
    }
}
