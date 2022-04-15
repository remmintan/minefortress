package org.minefortress.selections.renderer.selection;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.minefortress.renderer.custom.AbstractCustomBlockRenderView;

import java.util.function.Supplier;

public class SelectionBlockRenderView extends AbstractCustomBlockRenderView {

    private Supplier<BlockState> blockStateSupplier;

    public void setBlockStateSupplier(Supplier<BlockState> blockStateSupplier) {
        this.blockStateSupplier = blockStateSupplier;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return this.blockStateSupplier.get();
    }

    @Override
    public int getHeight() {
        return 900;
    }
}
