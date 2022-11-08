package org.minefortress.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;

public class ServerboundSleepPacket implements FortressServerPacket {

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
