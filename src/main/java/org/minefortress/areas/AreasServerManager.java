package org.minefortress.areas;

import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.network.c2s.S2CSyncAreasPacket;
import org.minefortress.network.helpers.FortressServerNetworkHelper;

import java.util.ArrayList;
import java.util.List;

public final class AreasServerManager {

    private boolean needSync = false;
    private final List<AutomationAreaInfo> areas = new ArrayList<>();

    public void addArea(AutomationAreaInfo area) {
        areas.add(area);
        sync();
    }

    public void tick(ServerPlayerEntity serverPlayer) {
        if(needSync) {
            FortressServerNetworkHelper.send(serverPlayer, S2CSyncAreasPacket.CHANNEL, new S2CSyncAreasPacket(areas));
            needSync = false;
        }
    }

    private void sync() {
        needSync = true;
    }

}
