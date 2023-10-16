package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

import java.util.UUID;

public class C2SCaptureInfluencePositionPacket implements FortressC2SPacket {

    public static final String CHANNEL = "capture_influence_position";

    private final UUID taskId;
    private final BlockPos pos;

    public C2SCaptureInfluencePositionPacket(UUID taskId, BlockPos pos) {
        this.taskId = taskId;
        this.pos = pos;
    }

    public C2SCaptureInfluencePositionPacket(PacketByteBuf buf) {
        this.taskId = buf.readUuid();
        this.pos = buf.readBlockPos();
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        getManagersProvider(server, player).getInfluenceManager().addCapturePosition(taskId, pos, player);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(taskId);
        buf.writeBlockPos(pos);
    }
}
