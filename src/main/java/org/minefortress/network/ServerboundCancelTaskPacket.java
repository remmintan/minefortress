package org.minefortress.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;
import org.minefortress.tasks.TaskManager;

import java.util.UUID;

public class ServerboundCancelTaskPacket implements FortressServerPacket {

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

    public UUID getTaskId() {
        return taskId;
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        UUID id = this.getTaskId();
        TaskManager taskManager = ((FortressServerPlayerEntity)player).getTaskManager();
        taskManager.cancelTask(id);
    }
}
