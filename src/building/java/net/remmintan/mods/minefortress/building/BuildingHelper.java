package net.remmintan.mods.minefortress.building;


import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.FlowerBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class BuildingHelper {

    public static boolean canPlaceBlock(@Nullable World level, @Nullable BlockPos pos) {
        if (level == null || pos == null) return false;
        final BlockState blockState = level.getBlockState(pos);
        return canPlaceBlock(level, blockState, pos);
    }

    public static boolean canPlaceBlock(@Nullable World level, @Nullable BlockState state, @Nullable BlockPos pos) {
        if (level == null || state == null || pos == null) return false;
        return inWorldBounds(level, pos) && (
                isAirOrFluid(state) ||
                isGrass(level, state, pos) ||
                state.isReplaceable() ||
//                doesNotHaveCollisions(level, pos) ||
                state.getBlock().equals(FortressBlocks.SCAFFOLD_OAK_PLANKS)
        );
    }

    public static boolean canRemoveBlock(@Nullable World level, @Nullable BlockPos pos) {
        if (level == null || pos == null) return false;
        final BlockState blockState = level.getBlockState(pos);
        return canRemoveBlock(level, blockState, pos);
    }

    public static boolean canRemoveBlock(@Nullable World level, @Nullable BlockState state, @Nullable BlockPos pos) {
        if (level == null || state == null || pos == null) return false;
        return inWorldBounds(level, pos) &&
                !isAirOrFluid(state) &&
                !state.isOf(Blocks.BEDROCK) &&
                !state.isOf(Blocks.BARRIER);
    }

    public static boolean canStayOnBlock(@Nullable WorldAccess level, @Nullable BlockPos pos) {
        if (level == null || pos == null) return false;
        final BlockState blockState = level.getBlockState(pos);
        return canStayOnBlock(level, blockState, pos);
    }

    public static boolean canStayOnBlock(@Nullable WorldAccess level, @Nullable BlockState state, @Nullable BlockPos pos) {
        if (level == null || state == null || pos == null) return false;
        return !isAirOrFluid(state) &&
                hasCollisions(level, pos) &&
                doesNotHaveCollisions(level, pos.up()) &&
                doesNotHaveCollisions(level, pos.up().up());
    }

    public static boolean doesNotHaveCollisions(@Nullable WorldAccess level, @Nullable BlockPos pos) {
        if (level == null || pos == null) return false;
        final BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos) == VoxelShapes.empty();
    }

    public static boolean hasCollisions(@Nullable WorldAccess level, @Nullable BlockPos pos) {
        if (level == null || pos == null) return false;
        final BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos) != VoxelShapes.empty();
    }

    private static boolean isGrass(World level, BlockState state, BlockPos pos) {
        final VoxelShape collisionShape = state.getCollisionShape(level, pos);
        return collisionShape == VoxelShapes.empty() && (state.getBlock() instanceof Fertilizable || state.getBlock() instanceof FlowerBlock);
    }

    private static boolean isAirOrFluid(BlockState state) {
        return state.isAir() || state.isOf(Blocks.WATER) || state.isOf(Blocks.LAVA);
    }

    private static boolean inWorldBounds(World level, BlockPos pos) {
        return level.isInBuildLimit(pos);
    }

}
