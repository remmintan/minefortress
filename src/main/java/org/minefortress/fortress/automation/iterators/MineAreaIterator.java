package org.minefortress.fortress.automation.iterators;

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
        return BuildingHelper.canRemoveBlock(world, pos);
    }

    @Override
    protected AutomationBlockInfo map(BlockPos pos) {
        return new AutomationBlockInfo(pos, AutomationActionType.MINE);
    }
}
