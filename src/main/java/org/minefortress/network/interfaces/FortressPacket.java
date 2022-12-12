package org.minefortress.network.interfaces;

import net.minecraft.network.PacketByteBuf;

public interface FortressPacket {

    void write(PacketByteBuf buf);

}
