package org.minefortress.fortress.automation.iterators;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.building.BuildingHelper;
import net.remmintan.mods.minefortress.core.automation.AutomationBlockInfo;
import net.remmintan.mods.minefortress.core.automation.iterators.AbstractFilteredIterator;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.AutomationActionType;

import java.util.List;

public class FarmAreaIterator extends AbstractFilteredIterator {

    private final World world;
    public FarmAreaIterator(List<BlockPos> areaBlocks, World world) {
        super(areaBlocks.listIterator());
        this.world = world;
    }

    public static boolean blockCanBeRemovedToPlantCrops(BlockState blockState) {
        return blockState.isOf(Blocks.GRASS) ||
                blockState.isIn(BlockTags.HOE_MINEABLE) ||
                blockState.isIn(BlockTags.FLOWERS);
    }

    @Override
    protected boolean filter(BlockPos pos) {
        final var topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING, pos.getX(), pos.getZ());
        final var posSuitableForWater = pos.getY() == topY && isGoalCorrectForWater(pos);
        if(topY -1 != pos.getY() && !posSuitableForWater) {
            return false;
        }

        final var blockState = world.getBlockState(pos);
        if(posSuitableForWater && blockState.isAir()) {
            return true;
        }

        final var goalCorrect = blockState.isOf(Blocks.FARMLAND) || blockState.isOf(Blocks.DIRT) || blockState.isOf(Blocks.GRASS_BLOCK) || isGoalCorrectForWater(pos);
        final var aboveGoalState = world.getBlockState(pos.up());
        final var aboveGoalCorrect = aboveGoalState.isIn(BlockTags.CROPS) || aboveGoalState.isAir() || blockCanBeRemovedToPlantCrops(aboveGoalState);
        return goalCorrect && aboveGoalCorrect;
    }

    @Override
    protected AutomationBlockInfo map(BlockPos pos) {
        final var actionType = isGoalCorrectForWater(pos) ? AutomationActionType.FARM_WATER : AutomationActionType.FARM_CROPS;
        return new AutomationBlockInfo(pos, actionType);
    }

    private boolean isGoalCorrectForWater(BlockPos pos) {
        return pos.getX() % 4 == 0
                && pos.getZ() % 4 == 0
                && positionSuitableForWater(pos.west())
                && positionSuitableForWater(pos.east())
                && positionSuitableForWater(pos.north())
                && positionSuitableForWater(pos.south());
    }

    private boolean positionSuitableForWater(BlockPos nearPos) {
        return BuildingHelper.hasCollisions(world, nearPos) ||  world.getBlockState(nearPos).getFluidState().isIn(FluidTags.WATER);
    }
}
