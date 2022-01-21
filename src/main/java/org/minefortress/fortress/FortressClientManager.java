package org.minefortress.fortress;

import net.minecraft.util.math.BlockPos;

public final class FortressClientManager {

    private boolean initialized = false;

    private BlockPos fortressCenter = null;
    private int colonistsCount = 0;

    public int getColonistsCount() {
        return colonistsCount;
    }

    public void sync(int colonistsCount, BlockPos fortressCenter) {
        this.colonistsCount = colonistsCount;
        this.fortressCenter = fortressCenter;
        initialized = true;
    }

    public void tick() {
        if(!initialized) return;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
