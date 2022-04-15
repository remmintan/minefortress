package org.minefortress.selections.renderer.campfire;

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
import org.minefortress.renderer.custom.AbstractCustomBlockRenderView;

public class CampfireRenderView extends AbstractCustomBlockRenderView {
    @Override
    public BlockState getBlockState(BlockPos pos) {
        return Blocks.CAMPFIRE.getDefaultState();
    }

    @Override
    public int getHeight() {
        return 1;
    }
}
