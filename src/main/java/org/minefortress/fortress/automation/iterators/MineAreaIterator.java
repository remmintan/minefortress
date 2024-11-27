package org.minefortress.fortress.automation.iterators;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.blocks.FortressBlocks;
import net.remmintan.mods.minefortress.building.BuildingHelper;
import net.remmintan.mods.minefortress.core.automation.AutomationBlockInfo;
import net.remmintan.mods.minefortress.core.automation.iterators.AbstractFilteredIterator;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.AutomationActionType;

import java.util.List;

public class MineAreaIterator extends AbstractFilteredIterator {

    private final World world;

    public MineAreaIterator(List<BlockPos> blocks, World world) {
        super(blocks.listIterator());
        this.world = world;
    }

    @Override
    protected boolean filter(BlockPos pos) {
        return BuildingHelper.canRemoveBlock(world, pos) &&
                notFluid(pos) &&
                noFluidAround(pos) &&
                notSkaffoldBlock(pos);
    }

    private boolean notSkaffoldBlock(BlockPos pos) {
        return !world.getBlockState(pos).isOf(FortressBlocks.SCAFFOLD_OAK_PLANKS);
    }

    @Override
    protected AutomationBlockInfo map(BlockPos pos) {
        return new AutomationBlockInfo(pos, AutomationActionType.MINE);
    }

    private boolean notFluid(BlockPos pos) {
        return world.getFluidState(pos).isEmpty();
    }

    private boolean noFluidAround(BlockPos pos) {
        return world.getFluidState(pos.up()).isEmpty()
                && world.getFluidState(pos.north()).isEmpty()
                && world.getFluidState(pos.south()).isEmpty()
                && world.getFluidState(pos.east()).isEmpty()
                && world.getFluidState(pos.west()).isEmpty();
    }

}
