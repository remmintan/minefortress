package org.minefortress.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.network.interfaces.FortressC2SPacket;

public class C2SJumpToCampfire implements FortressC2SPacket {

    public static final String CHANNEL = "jump_to_campfire";

    public C2SJumpToCampfire() {}

    public C2SJumpToCampfire(PacketByteBuf buf) {}

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var fortressServerManager = getFortressServerManager(server, player);
        fortressServerManager.jumpToCampfire(player);
    }

    @Override
    public void write(PacketByteBuf buf) {}
}
