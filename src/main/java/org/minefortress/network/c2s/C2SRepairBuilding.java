package org.minefortress.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.network.interfaces.FortressC2SPacket;

import java.util.UUID;

public class C2SRepairBuilding implements FortressC2SPacket {

    public static final String CHANNEL = "repair_building";

    private final UUID taskId;
    private final UUID buildingId;

    public C2SRepairBuilding(UUID taskId, UUID buildingId) {
        this.taskId = taskId;
        this.buildingId = buildingId;
    }

    public C2SRepairBuilding(PacketByteBuf buf) {
        taskId = buf.readUuid();
        buildingId = buf.readUuid();
    }


    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var serverManager = getFortressServerManager(server, player);
        serverManager.repairBuilding(player, taskId, buildingId);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(taskId);
        buf.writeUuid(buildingId);
    }
}
