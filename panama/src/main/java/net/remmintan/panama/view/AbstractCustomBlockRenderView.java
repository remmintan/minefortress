package net.remmintan.panama.view;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public abstract class AbstractCustomBlockRenderView implements BlockRenderView {

    private static final float LIGHT_LEVEL = 15f;
    private final BiFunction<BlockState, ColorResolver, Integer> colorProvider;

    public AbstractCustomBlockRenderView(BiFunction<BlockState, ColorResolver, Integer> colorProvider) {
        this.colorProvider = colorProvider;
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return  getMultiplyerFromDirection(direction);
    }

    private float getMultiplyerFromDirection(Direction direction) {
        return switch (direction) {
            case Direction.NORTH, Direction.EAST -> 0.5f;
            default -> 1f;
        };
    }

    @Override
    public LightingProvider getLightingProvider() {
        return null;
    }

    @Override
    public int getLightLevel(LightType type, BlockPos pos) {
        return (int)LIGHT_LEVEL;
    }

    @Override
    public int getBaseLightLevel(BlockPos pos, int ambientDarkness) {
        return (int)LIGHT_LEVEL;
    }

    @Override
    public int getColor(BlockPos pos, ColorResolver colorResolver) {
        return colorProvider != null ? colorProvider.apply(this.getBlockState(pos), colorResolver) : 0xffffff;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        final BlockState blockState = this.getBlockState(pos);
        final Block block = blockState.getBlock();
        if(block instanceof BlockEntityProvider) {
            return ((BlockEntityProvider) block).createBlockEntity(pos, blockState);
        }
        return null;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
    }

    @Override
    public int getBottomY() {
        return 0;
    }

}
