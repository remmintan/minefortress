package org.minefortress.network.interfaces;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public interface FortressServerPacket extends FortressPacket {

    void handle(MinecraftServer server, ServerPlayerEntity player);

}
