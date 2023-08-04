package org.minefortress.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.network.interfaces.FortressC2SPacket;
import org.minefortress.tasks.TaskManager;

import java.util.UUID;

public class ServerboundCancelTaskPacket implements FortressC2SPacket {

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
        final var fortressServerManager = this.getFortressServerManager(server, player);
        TaskManager taskManager = fortressServerManager.getTaskManager();
        taskManager.cancelTask(id, fortressServerManager);
    }
}
