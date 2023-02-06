package org.minefortress.fortress.automation.iterators;

import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.minefortress.fortress.automation.AutomationActionType;
import org.minefortress.fortress.automation.AutomationBlockInfo;
import org.minefortress.utils.BuildingHelper;

import java.util.List;

public class MineAreaIterator extends AbstractFilteredIterator{

    private final World world;

    public MineAreaIterator(List<BlockPos> blocks, World world) {
        super(blocks.listIterator());
        this.world = world;
    }

    @Override
    protected boolean filter(BlockPos pos) {
        return BuildingHelper.canRemoveBlock(world, pos) && notWater(pos) && noWaterAround(pos);
    }

    @Override
    protected AutomationBlockInfo map(BlockPos pos) {
        return new AutomationBlockInfo(pos, AutomationActionType.MINE);
    }

    private boolean notWater(BlockPos pos) {
        return !world.getFluidState(pos).isIn(FluidTags.WATER);
    }

    private boolean noWaterAround(BlockPos pos) {
        return world.getFluidState(pos.up()).isEmpty()
                && world.getFluidState(pos.north()).isEmpty()
                && world.getFluidState(pos.south()).isEmpty()
                && world.getFluidState(pos.east()).isEmpty()
                && world.getFluidState(pos.west()).isEmpty();
    }

}
