package org.minefortress.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import org.minefortress.blueprints.ServerBlueprintManager;
import org.minefortress.interfaces.FortressServerWorld;
import org.minefortress.network.interfaces.FortressServerPacket;
import org.minefortress.tasks.BlueprintTask;

import java.util.UUID;

public class ServerboundBlueprintTaskPacket implements FortressServerPacket {

    private final UUID taskId;
    private final String blueprintId;
    private final String blueprintFile;
    private final BlockPos startPos;
    private final BlockRotation rotation;

    public ServerboundBlueprintTaskPacket(UUID taskId, String blueprintId, String blueprintFile, BlockPos startPos, BlockRotation rotation) {
        this.taskId = taskId;
        this.blueprintId = blueprintId;
        this.blueprintFile = blueprintFile;
        this.startPos = startPos;
        this.rotation = rotation;
    }

    public ServerboundBlueprintTaskPacket(PacketByteBuf buf) {
        this.taskId = buf.readUuid();
        this.blueprintId = buf.readString();
        this.blueprintFile = buf.readString();
        this.startPos = buf.readBlockPos();
        this.rotation = buf.readEnumConstant(BlockRotation.class);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(taskId);
        buf.writeString(blueprintId);
        buf.writeString(blueprintFile);
        buf.writeBlockPos(startPos);
        buf.writeEnumConstant(rotation);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final ServerWorld serverWorld = player.getServerWorld();
        if(serverWorld instanceof final FortressServerWorld fortressWorld) {
            final ServerBlueprintManager blueprintManager = fortressWorld.getBlueprintManager();
            final BlueprintTask task = blueprintManager.createTask(taskId, blueprintId, blueprintFile, startPos, serverWorld, rotation);
            fortressWorld.getTaskManager().addTask(task);
        }
    }
}
