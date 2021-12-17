package org.minefortress.network.interfaces;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

import java.util.function.Consumer;

public interface FortressPacket {

    void write(PacketByteBuf buf);

}
