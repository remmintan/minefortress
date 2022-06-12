package org.minefortress.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;

import java.util.ArrayList;
import java.util.List;

public class ServerboundSelectColonistsPacket implements FortressServerPacket {

    private final List<Integer> colonistIds;

    public ServerboundSelectColonistsPacket(List<Integer> colonistIds) {
        this.colonistIds = colonistIds;
    }

    public ServerboundSelectColonistsPacket(PacketByteBuf buf) {
        final var count = buf.readInt();
        colonistIds = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            colonistIds.add(buf.readInt());
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(colonistIds.size());
        for (Integer id : colonistIds) {
            buf.writeInt(id);
        }
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var fortressServerManager = this.getFortressServerManager(server, player);
        fortressServerManager.selectColonists(colonistIds);
    }
}
