package org.minefortress.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class BlockUtils {

    public static BlockState getBlockStateFromItem(Item item) {
        if(item instanceof BlockItem) {
            return ((BlockItem)item).getBlock().getDefaultState();
        } else if(item == Items.WATER_BUCKET) {
            return Fluids.WATER.getDefaultState().getBlockState().with(FluidBlock.LEVEL, 8);
        } else if(item == Items.LAVA_BUCKET) {
            return Fluids.LAVA.getDefaultState().getBlockState().with(FluidBlock.LEVEL, 8);
        } else if(item == Items.FLINT_AND_STEEL) {
            return Blocks.FIRE.getDefaultState();
        }

        return null;
    }

    public static boolean isCountableBlock(BlockState state) {
        if(!state.getFluidState().isEmpty()) return false;
        final var block = state.getBlock();
        if(block == Blocks.FIRE) return false;
        if(block == Blocks.AIR) return false;
        if(block == Blocks.BARRIER) return false;

        return true;
    }

}
