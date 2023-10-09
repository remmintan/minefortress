package net.remmintan.mods.minefortress.networking.interfaces;

import net.minecraft.network.PacketByteBuf;

public interface FortressPacket {

    void write(PacketByteBuf buf);

}
