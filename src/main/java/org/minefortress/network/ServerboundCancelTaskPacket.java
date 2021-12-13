package org.minefortress.network;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import org.apache.commons.lang3.NotImplementedException;

import java.util.UUID;

public class ServerboundCancelTaskPacket implements Packet<ServerPlayPacketListener> {

    private final UUID taskId;

    public ServerboundCancelTaskPacket(UUID taskId) {
        this.taskId = taskId;
    }

    public ServerboundCancelTaskPacket(PacketByteBuf buf) {
        this.taskId = buf.readUuid();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(taskId);
    }

    @Override
    public void apply(ServerPlayPacketListener listener) {
        throw new NotImplementedException("ServerboundCancelTaskPacket.handle");
    }

    public UUID getTaskId() {
        return taskId;
    }
}
