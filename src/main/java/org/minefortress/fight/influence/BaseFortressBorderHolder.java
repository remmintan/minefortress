package org.minefortress.fight.influence;

import net.minecraft.util.math.BlockPos;

public class BaseFortressBorderHolder {

    public static final int FORTRESS_BORDER_SIZE = 64;

    public static BlockPos alignToAGrid(BlockPos pos) {
        var x = pos.getX() - (pos.getX() % ClientFortressBorderHolder.FORTRESS_BORDER_SIZE);
        var z = pos.getZ() - (pos.getZ() % ClientFortressBorderHolder.FORTRESS_BORDER_SIZE);
        return new BlockPos(x, 0, z);
    }

}
