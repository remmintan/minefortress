package org.minefortress.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.network.interfaces.FortressC2SPacket;

import java.util.UUID;

public class C2SRepairBuilding implements FortressC2SPacket {

    private UUID buildingId;

    public C2SRepairBuilding(UUID buildingId) {
        this.buildingId = buildingId;
    }

    public C2SRepairBuilding(PacketByteBuf buf) {
        buildingId = buf.readUuid();
    }


    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var serverManager = getFortressServerManager(server, player);
        serverManager.repairBuilding(buildingId);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(buildingId);
    }
}
