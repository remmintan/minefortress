package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.networking.interfaces.FortressC2SPacket;

public class ServerboundSleepPacket implements FortressC2SPacket {

    public ServerboundSleepPacket() {
    }

    public ServerboundSleepPacket(PacketByteBuf buf) {
    }

    @Override
    public void write(PacketByteBuf buf) {}

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(!player.isSleeping()) {
            player.trySleep(player.getBlockPos()).ifLeft((reason) -> {
                if (reason != null && reason.getMessage() != null) {
                    player.sendMessage(reason.getMessage(), true);
                }
            });
        }
    }
}
