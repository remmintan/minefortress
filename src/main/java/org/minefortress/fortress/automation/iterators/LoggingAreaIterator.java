package org.minefortress.fortress.automation.iterators;

import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.automation.AutomationBlockInfo;
import net.remmintan.mods.minefortress.core.automation.iterators.AbstractFilteredIterator;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.AutomationActionType;

import java.util.*;

public class LoggingAreaIterator extends AbstractFilteredIterator {

    private final World world;

    private final Map<BlockPos, AutomationBlockInfo> cache = new HashMap<>();
    private final Set<BlockPos> existingSaplings = new HashSet<>();

    public LoggingAreaIterator(List<BlockPos> blocks, World world) {
        super(blocks.listIterator());
        this.world=world;
    }

    @Override
    protected boolean filter(BlockPos pos) {
        final var topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        final var newPos = new BlockPos(pos.getX(), topY, pos.getZ());

        final var root = getTreeRoot(newPos);
        if(root.isPresent()) {
            cache.put(pos, new AutomationBlockInfo(root.get(), AutomationActionType.CHOP_TREE));
            return true;
        }

        if(noOtherTreesOrSaplingsAround(newPos) && world.getBlockState(newPos.down()).isIn(BlockTags.DIRT)) {
            existingSaplings.add(newPos);
            cache.put(pos, new AutomationBlockInfo(newPos, AutomationActionType.PLANT_SAPLING));
            return true;
        }

        return  false;
    }

    @Override
    protected AutomationBlockInfo map(BlockPos pos) {
        return cache.get(pos);
    }

    private static Optional<BlockPos> findRootDownFromLog(BlockPos start, World world) {
        BlockPos cursor = start;
        BlockState cursorState;

        do {
            cursor = cursor.down();
            cursorState = world.getBlockState(cursor);
        } while (cursorState.isIn(BlockTags.LOGS));

        if (cursorState.isAir()) return Optional.empty();
        return Optional.of(cursor.up());
    }

    private Optional<BlockPos> getTreeRoot(BlockPos pos) {
        if(!world.getBlockState(pos.down()).isIn(BlockTags.LOGS)) {
            return Optional.empty();
        }

        return findRootDownFromLog(pos, world);
    }


    private boolean noOtherTreesOrSaplingsAround(BlockPos pos) {
        final var state = world.getBlockState(pos);
        if(!state.isAir() && !state.isIn(BlockTags.REPLACEABLE)) return false;

        final var start = pos.add(-3, -5, -3);
        final var end = pos.add(3, 5, 3);

        for (BlockPos blockPos : BlockPos.iterate(start, end)) {
            final var blockState = world.getBlockState(blockPos);
            if (
                    blockState.isIn(BlockTags.LOGS) ||
                    blockState.isIn(BlockTags.SAPLINGS) ||
                    blockState.isIn(BlockTags.LEAVES) ||
                    existingSaplings.contains(blockPos)
            ) {
                return false;
            }
        }

        return true;
    }
}
