package org.minefortress.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

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

    public static boolean isPosBetween(BlockPos pos, BlockPos start, BlockPos end) {
        return pos.getX() >= start.getX() && pos.getX() <= end.getX() &&
                pos.getY() >= start.getY() && pos.getY() <= end.getY() &&
                pos.getZ() >= start.getZ() && pos.getZ() <= end.getZ();
    }

}
