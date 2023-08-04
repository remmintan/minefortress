package org.minefortress.selections.renderer.selection;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.ColorResolver;
import org.minefortress.renderer.custom.AbstractCustomBlockRenderView;

import java.util.function.BiFunction;
import java.util.function.Function;

public class SelectionBlockRenderView extends AbstractCustomBlockRenderView {

    private Function<BlockPos, BlockState> blockStateSupplier;

    public SelectionBlockRenderView(BiFunction<BlockState, ColorResolver, Integer> colorProvider) {
        super(colorProvider);
    }

    public void setBlockStateSupplier(Function<BlockPos, BlockState> blockStateSupplier) {
        this.blockStateSupplier = blockStateSupplier;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return this.blockStateSupplier.apply(pos);
    }

    @Override
    public int getHeight() {
        return 900;
    }
}
