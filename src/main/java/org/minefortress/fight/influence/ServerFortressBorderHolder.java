package org.minefortress.fight.influence;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorderStage;

import java.util.HashSet;
import java.util.Set;

class ServerFortressBorderHolder extends BaseFortressBorderHolder{

    private final Set<BlockPos> allInfluencePositionsAlignedToAGrid = new HashSet<>();

    public void add(BlockPos pos) {
        allInfluencePositionsAlignedToAGrid.add(alignToAGrid(pos));
    }

    public WorldBorderStage getStage(BlockPos pos) {
        var alignedPos = alignToAGrid(pos);
        if(allInfluencePositionsAlignedToAGrid.contains(alignedPos))
            return WorldBorderStage.STATIONARY;
        final var blockPos = alignedPos.toImmutable();
        for (var offset : new int[][]{{-1, 0}, {0, -1}, {1, 0}, {0, 1}, {-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
            var neighbour = blockPos.add(
                    offset[0] * FORTRESS_BORDER_SIZE,
                    0,
                    offset[1] * FORTRESS_BORDER_SIZE
            );
            if (allInfluencePositionsAlignedToAGrid.contains(neighbour))
                return WorldBorderStage.GROWING;
        }
        return WorldBorderStage.SHRINKING;
    }

    public void clear() {
        allInfluencePositionsAlignedToAGrid.clear();
    }
}
