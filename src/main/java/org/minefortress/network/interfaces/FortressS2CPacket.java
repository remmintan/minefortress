package org.minefortress.network.interfaces;

import net.minecraft.client.MinecraftClient;

public interface FortressS2CPacket extends FortressPacket{

    void handle(MinecraftClient client);

}
