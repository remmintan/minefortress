package org.minefortress.fight.influence;

import net.minecraft.util.math.BlockPos;

public class BaseFortressBorderHolder {

    public static final int FORTRESS_BORDER_SIZE = 64;

    static BlockPos alignToAGrid(BlockPos center) {
        final var x = center.getX();
        final var z = center.getZ();
        final var xSign = Math.signum(x);
        final var zSign = Math.signum(z);
        final var nonZeroSignX = xSign == 0 ? 1 : xSign;
        final var nonZeroSignZ = zSign == 0 ? 1 : zSign;
        final var adjustedX = x - x % FORTRESS_BORDER_SIZE + nonZeroSignX * FORTRESS_BORDER_SIZE / 2f;
        final var adjustedZ = z - z % FORTRESS_BORDER_SIZE + nonZeroSignZ * FORTRESS_BORDER_SIZE / 2f;
        return new BlockPos(adjustedX, 0, adjustedZ);
    }

}
