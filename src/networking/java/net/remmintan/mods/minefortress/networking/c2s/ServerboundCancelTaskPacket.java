package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import org.jetbrains.annotations.NotNull;

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
    public void handle(@NotNull MinecraftServer server, @NotNull ServerPlayerEntity player) {
        final var id = this.getTaskId();
        final var provider = getManagersProvider(player);
        final var taskManager = provider.getTaskManager();
        taskManager.cancelTask(id, player);
    }
}
