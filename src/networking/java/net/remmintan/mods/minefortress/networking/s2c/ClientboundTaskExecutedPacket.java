package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksInformationHolder;

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
        final ITasksInformationHolder world = (ITasksInformationHolder) client.world;
        if (world != null) {
            world.get_ClientTasksHolder().removeTask(taskId);
        }
    }
}
