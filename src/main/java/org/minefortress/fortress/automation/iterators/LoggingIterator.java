package org.minefortress.fortress.automation.iterators;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.remmintan.gobi.helpers.TreeHelper;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.AutomationActionType;
import org.minefortress.fortress.automation.AutomationBlockInfo;

import java.util.List;
import java.util.Optional;

public class LoggingIterator extends AbstractFilteredIterator {

    private final World world;

    public LoggingIterator(List<BlockPos> blocks, World world) {
        super(blocks.listIterator());
        this.world=world;
    }

    @Override
    protected boolean filter(BlockPos pos) {
        return getTreeRoot(pos).isPresent();
    }

    @Override
    protected AutomationBlockInfo map(BlockPos pos) {
        return new AutomationBlockInfo(getTreeRoot(pos).get(), AutomationActionType.CHOP_TREE);
    }

    private Optional<BlockPos> getTreeRoot(BlockPos pos) {
        final var topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        final var newPos = new BlockPos(pos.getX(), topY, pos.getZ());

        return TreeHelper.findRootDownFromLog(newPos, world);
    }

}
