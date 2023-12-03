package org.minefortress.fortress.automation.iterators;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.remmintan.gobi.helpers.TreeHelper;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.AutomationActionType;
import org.minefortress.fortress.automation.AutomationBlockInfo;

import java.util.List;

public class LoggingIterator extends AbstractFilteredIterator {

    private final World world;

    public LoggingIterator(List<BlockPos> blocks, World world) {
        super(blocks.listIterator());
        this.world=world;
    }

    @Override
    protected boolean filter(BlockPos pos) {
        final var topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        final var newPos = new BlockPos(pos.getX(), topY, pos.getZ());

        final var treeRoot = TreeHelper.findRootDownFromLog(newPos, world);

        return treeRoot.isPresent();
    }

    @Override
    protected AutomationBlockInfo map(BlockPos pos) {
        return new AutomationBlockInfo(pos, AutomationActionType.CHOP_TREE);
    }
}
