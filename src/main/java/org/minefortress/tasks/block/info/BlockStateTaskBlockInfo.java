package org.minefortress.tasks.block.info;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class BlockStateTaskBlockInfo extends TaskBlockInfo{

    private final BlockState state;

    public BlockStateTaskBlockInfo(BlockItem placingItem, BlockPos pos) {
        this(placingItem, pos, placingItem.getBlock().getDefaultState());
    }

    public BlockStateTaskBlockInfo(Item placingItem, BlockPos pos, BlockState state) {
        super(placingItem, pos);
        this.state = state;
    }

    public BlockState getState() {
        return state;
    }

}
