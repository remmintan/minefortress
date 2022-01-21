package org.minefortress.fortress;

import net.minecraft.util.math.BlockPos;
import org.minefortress.network.ServerboundFortressCenterSetPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;

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

    public boolean isFortressInitializationNeeded() {
        return initialized && fortressCenter == null;
    }

    public void setupFortressCenter(BlockPos center) {
        if(fortressCenter!=null) throw new IllegalStateException("Fortress center already set");
        fortressCenter = center;
        final ServerboundFortressCenterSetPacket serverboundFortressCenterSetPacket = new ServerboundFortressCenterSetPacket(center);
        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_SET_CENTER, serverboundFortressCenterSetPacket);
    }
}
