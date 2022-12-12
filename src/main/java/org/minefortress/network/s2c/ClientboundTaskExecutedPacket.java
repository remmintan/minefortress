package org.minefortress.network.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.network.interfaces.FortressS2CPacket;

import java.util.UUID;

public class ClientboundTaskExecutedPacket implements FortressS2CPacket {

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
    public void handle(MinecraftClient client) {
        UUID taskId = this.getTaskId();
        final FortressClientWorld world = (FortressClientWorld) client.world;
        if (world != null) {
            world.getClientTasksHolder().removeTask(taskId);
        }
    }
}
