package org.minefortress.professions;

import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.dtos.professions.IProfessionEssentialInfo;
import net.remmintan.mods.minefortress.core.interfaces.networking.INetworkingReader;

public class ProfessionsEssentialInfoReader implements INetworkingReader<IProfessionEssentialInfo> {
    @Override
    public IProfessionEssentialInfo readBuffer(PacketByteBuf buf) {
        return new ProfessionEssentialInfo(buf.readString(), buf.readInt());
    }

    @Override
    public boolean canReadForType(Class<?> type) {
        return type.isAssignableFrom(IProfessionEssentialInfo.class);
    }
}
