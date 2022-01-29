package org.minefortress.blueprints.renderer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.level.ColorResolver;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

class BlueprintBlockRenderView implements BlockRenderView {

    private final Map<BlockPos, BlockState> blueprintData;

    public BlueprintBlockRenderView(Map<BlockPos, BlockState> blueprintData) {
        this.blueprintData = blueprintData;
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return this.getMaxLightLevel();
    }

    @Override
    public LightingProvider getLightingProvider() {
        return null;
    }

    @Override
    public int getLightLevel(LightType type, BlockPos pos) {
        return this.getMaxLightLevel();
    }

    @Override
    public int getBaseLightLevel(BlockPos pos, int ambientDarkness) {
        return this.getMaxLightLevel();
    }

    @Override
    public int getColor(BlockPos pos, ColorResolver colorResolver) {
        return 0xffffff;
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
    public BlockState getBlockState(BlockPos pos) {
        return this.blueprintData.getOrDefault(pos, Blocks.AIR.getDefaultState());
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public int getBottomY() {
        return 0;
    }
}
