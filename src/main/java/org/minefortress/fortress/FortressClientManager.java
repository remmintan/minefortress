package org.minefortress.fortress;

import net.minecraft.util.math.BlockPos;

public final class FortressClientManager {

    private BlockPos fortressCenter = null;
    private int colonistsCount = 0;

    public int getColonistsCount() {
        return colonistsCount;
    }

    public void sync(int colonistsCount, BlockPos fortressCenter) {
        this.colonistsCount = colonistsCount;
        this.fortressCenter = fortressCenter;
    }

    public void tick() {

    }

}
