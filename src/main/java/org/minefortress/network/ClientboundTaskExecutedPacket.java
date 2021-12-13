package org.minefortress.network;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import org.apache.commons.lang3.NotImplementedException;

import java.util.UUID;

public class ClientboundTaskExecutedPacket implements Packet<ClientPlayPacketListener> {

    private final UUID taskId;

    public ClientboundTaskExecutedPacket(UUID taskId) {
        this.taskId = taskId;
    }

    public ClientboundTaskExecutedPacket(PacketByteBuf buffer) {
        this.taskId = buffer.readUuid();
    }

    public UUID getTaskId() {
        return taskId;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(taskId);
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        throw new NotImplementedException("TODO");
    }

}
