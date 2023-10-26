package net.remmintan.mods.minefortress.core.interfaces.networking;

import net.minecraft.network.PacketByteBuf;

public interface FortressPacket {

    void write(PacketByteBuf buf);

}
