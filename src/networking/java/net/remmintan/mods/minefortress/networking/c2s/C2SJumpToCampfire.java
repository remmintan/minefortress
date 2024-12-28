package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

public class C2SJumpToCampfire implements FortressC2SPacket {

    public static final String CHANNEL = "jump_to_campfire";

    public C2SJumpToCampfire() {}

    public C2SJumpToCampfire(PacketByteBuf ignoredBuf) {}

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var fortressServerManager = getFortressManager(player);
        fortressServerManager.jumpToCampfire(player);
    }

    @Override
    public void write(PacketByteBuf buf) {}
}
