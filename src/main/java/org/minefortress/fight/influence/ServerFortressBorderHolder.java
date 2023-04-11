package org.minefortress.fight.influence;

import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

class ServerFortressBorderHolder extends BaseFortressBorderHolder{

    private final Set<BlockPos> allInfluencePositionsAlignedToAGrid = new HashSet<>();

    public void add(BlockPos pos) {
        allInfluencePositionsAlignedToAGrid.add(alignToAGrid(pos));
    }

    public boolean contains(BlockPos pos) {
        return allInfluencePositionsAlignedToAGrid.contains(alignToAGrid(pos));
    }

    public void clear() {
        allInfluencePositionsAlignedToAGrid.clear();
    }
}
