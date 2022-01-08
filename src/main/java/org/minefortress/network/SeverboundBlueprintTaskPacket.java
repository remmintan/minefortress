package org.minefortress.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.minefortress.blueprints.ServerBlueprintManager;
import org.minefortress.interfaces.FortressServerWorld;
import org.minefortress.network.interfaces.FortressServerPacket;
import org.minefortress.tasks.BlueprintTask;

import java.util.UUID;

public class SeverboundBlueprintTaskPacket implements FortressServerPacket {

    private final UUID taskId;
    private final String blueprintId;
    private final BlockPos startPos;

    public SeverboundBlueprintTaskPacket(UUID taskId, String blueprintId, BlockPos startPos) {
        this.taskId = taskId;
        this.blueprintId = blueprintId;
        this.startPos = startPos;
    }

    public SeverboundBlueprintTaskPacket(PacketByteBuf buf) {
        this.taskId = buf.readUuid();
        this.blueprintId = buf.readString();
        this.startPos = buf.readBlockPos();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(taskId);
        buf.writeString(blueprintId);
        buf.writeBlockPos(startPos);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final ServerWorld serverWorld = player.getServerWorld();
        if(serverWorld instanceof final FortressServerWorld fortressWorld) {
            final ServerBlueprintManager blueprintManager = fortressWorld.getBlueprintManager();
            final BlueprintTask task = blueprintManager.createTask(taskId, blueprintId, startPos, serverWorld);
            fortressWorld.getTaskManager().addTask(task);
        }
    }
}
