package org.minefortress.fortress.buildings;

import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IEssentialBuildingInfo;
import net.remmintan.mods.minefortress.core.interfaces.networking.INetworkingReader;

public class EssentialBuildingInfoReader implements INetworkingReader<IEssentialBuildingInfo> {
    @Override
    public IEssentialBuildingInfo readBuffer(PacketByteBuf buf) {
        return new EssentialBuildingInfo(buf);
    }

    @Override
    public boolean canReadForType(Class<?> type) {
        return type.isAssignableFrom(IEssentialBuildingInfo.class);
    }
}
