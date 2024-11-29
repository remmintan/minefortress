package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

public class C2SOpenRepairBuildingScreen implements FortressC2SPacket {

    public static final String CHANNEL = "open-repair-building-screen";

    private final BlockPos pos;

    public C2SOpenRepairBuildingScreen(BlockPos pos) {
        this.pos = pos;
    }

    public C2SOpenRepairBuildingScreen(PacketByteBuf buf) {
        this(BlockPos.fromLong(buf.readLong()));
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        getManagersProvider(server, player)
                .getBuildingsManager()
                .doRepairConfirmation(pos, player);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeLong(pos.asLong());
    }
}
