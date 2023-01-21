package org.minefortress.fortress.automation.iterators;

import net.minecraft.block.Blocks;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FarmBuildingIterator extends AbstractFilteredIterator {

    private final World world;

    public FarmBuildingIterator(BlockPos start, BlockPos end, World world) {
        super(BlockPos.iterate(start, end).iterator());
        this.world = world;
    }

    @Override
    protected boolean getFilter(BlockPos pos) {
        final var blockState = world.getBlockState(pos);
        final var goalCorrect = blockState.isOf(Blocks.FARMLAND) || blockState.isOf(Blocks.DIRT) || blockState.isOf(Blocks.GRASS_BLOCK);
        final var aboveGoalState = world.getBlockState(pos.up());
        final var aboveGoalCorrect = aboveGoalState.isIn(BlockTags.CROPS) || aboveGoalState.isAir();
        return goalCorrect && aboveGoalCorrect;
    }
}
