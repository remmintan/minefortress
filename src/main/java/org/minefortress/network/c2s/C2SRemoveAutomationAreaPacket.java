package org.minefortress.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.network.interfaces.FortressC2SPacket;

import java.util.UUID;

public class C2SRemoveAutomationAreaPacket implements FortressC2SPacket {

    public static final String CHANNEL = "c2s_remove_area";
    private final UUID id;

    public C2SRemoveAutomationAreaPacket(UUID id) {
        this.id = id;
    }

    public C2SRemoveAutomationAreaPacket(PacketByteBuf buf) {
        this.id = buf.readUuid();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(id);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        getFortressServerManager(server, player).getAreasManager().removeArea(id);
    }
}
