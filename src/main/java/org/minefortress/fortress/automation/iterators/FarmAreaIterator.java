package org.minefortress.fortress.automation.iterators;

import net.minecraft.block.Blocks;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.minefortress.fortress.automation.AutomationActionType;
import org.minefortress.fortress.automation.AutomationBlockInfo;

import java.util.List;

public class FarmAreaIterator extends AbstractFilteredIterator{

    private final World world;
    public FarmAreaIterator(List<BlockPos> areaBlocks, World world) {
        super(areaBlocks.listIterator());
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
        final var actionType = pos.getX() % 4 == 0 ? AutomationActionType.FARM_WATER : AutomationActionType.FARM_CROPS;
        return new AutomationBlockInfo(pos, actionType);
    }
}
