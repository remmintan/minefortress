package org.minefortress.blueprints.renderer;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.level.ColorResolver;
import org.minefortress.renderer.custom.AbstractCustomBlockRenderView;

import java.util.Map;
import java.util.function.BiFunction;

class BlueprintBlockRenderView extends AbstractCustomBlockRenderView {

    private final Map<BlockPos, BlockState> blueprintData;

    public BlueprintBlockRenderView(Map<BlockPos, BlockState> blueprintData, BiFunction<BlockState, ColorResolver, Integer> colorProvider) {
        super(colorProvider);
        this.blueprintData = blueprintData;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return this.blueprintData.getOrDefault(pos, Blocks.AIR.getDefaultState());
    }

    @Override
    public int getHeight() {
        return 16;
    }

}
