package org.minefortress.tasks;


import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.minefortress.registries.FortressBlocks;

public class BuildingManager {

    public static boolean canPlaceBlock(World level, BlockPos pos) {
        final BlockState blockState = level.getBlockState(pos);
        return canPlaceBlock(level, blockState, pos);
    }

    public static boolean canPlaceBlock(World level, BlockState state, BlockPos pos) {
        return inWorldBounds(level, pos) && (
                isAirOrFluid(state) ||
                isGrass(level, state, pos) ||
                doesNotHaveCollisions(level, pos) ||
                state.getBlock().equals(FortressBlocks.SCAFFOLD_OAK_PLANKS)
        );
    }

    public static boolean canRemoveBlock(World level, BlockPos pos) {
        final BlockState blockState = level.getBlockState(pos);
        return canRemoveBlock(level, blockState, pos);
    }

    public static boolean canRemoveBlock(World level, BlockState state, BlockPos pos) {
        return inWorldBounds(level, pos) &&
                !isAirOrFluid(state) &&
                !state.isOf(Blocks.BEDROCK);
    }

    public static boolean canStayOnBlock(WorldAccess level, BlockPos pos) {
        final BlockState blockState = level.getBlockState(pos);
        return canStayOnBlock(level, blockState, pos);
    }

    public static boolean canStayOnBlock(WorldAccess level, BlockState state, BlockPos pos) {
        return !isAirOrFluid(state) &&
                hasCollisions((WorldAccess)level, pos) &&
                doesNotHaveCollisions((WorldAccess)level, pos.up()) &&
                doesNotHaveCollisions((WorldAccess)level, pos.up().up());
    }

    public static boolean canGoUpOnBlock(WorldAccess level, BlockPos pos) {
        final BlockState blockState = level.getBlockState(pos);
        return canGoUpOnBlock(level, blockState, pos);
    }

    public static boolean canGoUpOnBlock(WorldAccess level, BlockState state, BlockPos pos) {
        return doesNotHaveCollisions(level, pos) &&
                doesNotHaveCollisions((WorldAccess)level, pos.up()) &&
                doesNotHaveCollisions((WorldAccess)level, pos.up().up());
    }

    public static boolean doesNotHaveCollisions(WorldAccess level, BlockPos pos) {
        final BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos) == VoxelShapes.empty();
    }

    private static boolean hasCollisions(WorldAccess level, BlockPos pos) {
        final BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos) != VoxelShapes.empty();
    }

    private static boolean isGrass(World level, BlockState state, BlockPos pos) {
        final VoxelShape collisionShape = state.getCollisionShape(level, pos);
        return collisionShape == VoxelShapes.empty() && state.getBlock() instanceof Fertilizable;
    }

    private static boolean isAirOrFluid(BlockState state) {
        return state.isAir() || state.isOf(Blocks.WATER) || state.isOf(Blocks.LAVA);
    }

    private static boolean inWorldBounds(World level, BlockPos pos) {
        return level.isInBuildLimit(pos);
    }

}
