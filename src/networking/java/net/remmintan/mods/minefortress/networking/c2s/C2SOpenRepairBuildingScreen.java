package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

import java.util.UUID;

public class C2SOpenRepairBuildingScreen implements FortressC2SPacket {

    public static final String CHANNEL = "open-repair-building-screen";

    private final UUID buildingId;

    public C2SOpenRepairBuildingScreen(PacketByteBuf buf) {
        this.buildingId = buf.readUuid();
    }

    public C2SOpenRepairBuildingScreen(UUID buildingId) {
        this.buildingId = buildingId;
    }


    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        getManagersProvider(server, player)
                .getBuildingsManager()
                .doRepairConfirmation(buildingId, player);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(buildingId);
    }
}
