package org.minefortress.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.network.interfaces.FortressC2SPacket;

import java.util.UUID;

public class C2SDestroyBuilding implements FortressC2SPacket {

    public final static String CHANNEL = "destroy_building";

    private final UUID buildingId;

    public C2SDestroyBuilding(UUID buildingId) {
        this.buildingId = buildingId;
    }

    public C2SDestroyBuilding(PacketByteBuf buf) {
        this.buildingId = buf.readUuid();
    }


    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        getFortressServerManager(server, player).destroyBuilding(buildingId);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(buildingId);
    }
}
