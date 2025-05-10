package net.remmintan.mods.minefortress.core.utils;


import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class BuildingHelper {

    public static boolean canPlaceBlock(@Nullable World level, @Nullable BlockPos pos) {
        if (level == null || pos == null) return false;
        final BlockState state = level.getBlockState(pos);
        if (state == null) return false;
        return level.isInBuildLimit(pos) && state.isReplaceable();
    }

    public static boolean canRemoveBlock(@Nullable World level, @Nullable BlockPos pos) {
        if (level == null || pos == null) return false;
        final var state = level.getBlockState(pos);
        if (state == null) return false;
        return level.isInBuildLimit(pos) &&
                notAirOrFluid(state) &&
                state.getHardness(level, pos) >= 0;
    }

    public static boolean canStayOnBlock(@Nullable WorldAccess level, @Nullable BlockPos pos) {
        if (level == null || pos == null) return false;
        pos = pos.toImmutable();
        final BlockState state = level.getBlockState(pos);
        if (state == null) return false;
        return notAirOrFluid(state) &&
                hasCollisions(level, pos) &&
                !hasCollisions(level, pos.up()) &&
                !hasCollisions(level, pos.up().up());
    }

    public static boolean hasCollisions(@Nullable WorldAccess level, @Nullable BlockPos pos) {
        if (level == null || pos == null) return false;
        final BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos) != VoxelShapes.empty();
    }

    private static boolean notAirOrFluid(BlockState state) {
        return !state.isAir() && !state.isOf(Blocks.WATER) && !state.isOf(Blocks.LAVA);
    }

}
