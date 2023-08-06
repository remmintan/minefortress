package org.minefortress.fortress.automation.iterators;


import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.minefortress.fortress.automation.AutomationActionType;
import org.minefortress.fortress.automation.AutomationBlockInfo;

public class FarmBuildingIterator extends AbstractFilteredIterator {

    private final World world;

    public FarmBuildingIterator(BlockPos start, BlockPos end, World world) {
        super(BlockPos.iterate(start, end).iterator());
        this.world = world;
    }

    @Override
    protected boolean filter(BlockPos pos) {
        final var blockState = world.getBlockState(pos);
        final var goalCorrect = blockState.isOf(Blocks.FARMLAND) || blockState.isOf(Blocks.DIRT) || blockState.isOf(Blocks.GRASS_BLOCK);
        final var aboveGoalState = world.getBlockState(pos.up());
        final var aboveGoalCorrect = aboveGoalState.isIn(BlockTags.CROPS) || aboveGoalState.isAir();
        return goalCorrect && aboveGoalCorrect;
    }

    @Override
    protected AutomationBlockInfo map(BlockPos pos) {
        return new AutomationBlockInfo(pos, AutomationActionType.FARM_CROPS);
    }
}
