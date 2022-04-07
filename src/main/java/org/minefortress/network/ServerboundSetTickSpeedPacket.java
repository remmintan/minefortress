package org.minefortress.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.interfaces.FortressServer;
import org.minefortress.network.interfaces.FortressServerPacket;

public class ServerboundSetTickSpeedPacket implements FortressServerPacket {

    private final int ticksSpeed;

    public ServerboundSetTickSpeedPacket(int ticksSpeed) {
        this.ticksSpeed = ticksSpeed;
    }

    public ServerboundSetTickSpeedPacket(PacketByteBuf buf) {
        ticksSpeed = buf.readInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(ticksSpeed);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final FortressServer fortressServer = (FortressServer) server;
        fortressServer.setTicksMultiplier(ticksSpeed);
    }
}
