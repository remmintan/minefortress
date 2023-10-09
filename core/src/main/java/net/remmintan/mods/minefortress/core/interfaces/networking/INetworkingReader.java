package net.remmintan.mods.minefortress.core.interfaces.networking;

import net.minecraft.network.PacketByteBuf;

public interface INetworkingReader<T> {

    T readBuffer(PacketByteBuf buf);
    boolean canReadForType(Class<?> type);

}
