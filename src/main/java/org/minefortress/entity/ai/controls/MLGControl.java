package org.minefortress.entity.ai.controls;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.minefortress.entity.Colonist;

import java.util.List;

public class MLGControl extends ActionControl {

    private final Colonist colonist;

    private static final Item MLG_ITEM = Items.WATER_BUCKET;
    private static final BlockState MLG_WATER_STATE = Blocks.WATER.getDefaultState().with(FluidBlock.LEVEL, 1);

    public MLGControl(Colonist colonist) {
        this.colonist = colonist;
    }

    @Override
    protected BlockPos doAction() {
        BlockPos pos = colonist.getBlockPos();
        while (colonist.world.getBlockState(pos).isAir())
            pos = pos.down();
        pos = pos.up();

        colonist.putItemInHand(MLG_ITEM);
        colonist.world.setBlockState(pos, MLG_WATER_STATE, 3);

        return pos;
    }

    @Override
    protected void clearResults(List<BlockPos> results) {
        final World level = colonist.world;
        for (BlockPos pos : results){
            if(level.getBlockState(pos).isOf(Blocks.WATER)){
                level.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            }
        }
    }
}
