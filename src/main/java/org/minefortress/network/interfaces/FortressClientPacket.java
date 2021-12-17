package org.minefortress.network.interfaces;

import net.minecraft.client.MinecraftClient;

public interface FortressClientPacket extends FortressPacket{

    void handle(MinecraftClient client);

}
