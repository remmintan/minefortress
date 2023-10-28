package net.remmintan.panama.view;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class CampfireRenderView extends AbstractCustomBlockRenderView {
    public CampfireRenderView() {
        super(null);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return Blocks.CAMPFIRE.getDefaultState();
    }

    @Override
    public int getHeight() {
        return 1;
    }
}
