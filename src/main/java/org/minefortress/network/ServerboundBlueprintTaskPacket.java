package org.minefortress.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import org.minefortress.blueprints.manager.ServerBlueprintManager;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.interfaces.FortressServerWorld;
import org.minefortress.network.interfaces.FortressServerPacket;
import org.minefortress.tasks.BlueprintTask;
import org.minefortress.tasks.SimpleSelectionTask;
import org.minefortress.tasks.TaskType;

import java.util.UUID;

public class ServerboundBlueprintTaskPacket implements FortressServerPacket {

    private final UUID taskId;
    private final String blueprintId;
    private final String blueprintFile;
    private final BlockPos startPos;
    private final BlockRotation rotation;
    private final int floorLevel;

    public ServerboundBlueprintTaskPacket(UUID taskId, String blueprintId, String blueprintFile, BlockPos startPos, BlockRotation rotation, int floorLevel) {
        this.taskId = taskId;
        this.blueprintId = blueprintId;
        this.blueprintFile = blueprintFile;
        this.startPos = startPos;
        this.rotation = rotation;
        this.floorLevel = floorLevel;
    }

    public ServerboundBlueprintTaskPacket(PacketByteBuf buf) {
        this.taskId = buf.readUuid();
        this.blueprintId = buf.readString();
        this.blueprintFile = buf.readString();
        this.startPos = buf.readBlockPos();
        this.rotation = buf.readEnumConstant(BlockRotation.class);
        this.floorLevel = buf.readInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(taskId);
        buf.writeString(blueprintId);
        buf.writeString(blueprintFile);
        buf.writeBlockPos(startPos);
        buf.writeEnumConstant(rotation);
        buf.writeInt(floorLevel);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(player instanceof final FortressServerPlayerEntity fortressServerPlayer) {
            final ServerBlueprintManager blueprintManager = fortressServerPlayer.getServerBlueprintManager();
            final BlueprintTask task = blueprintManager.createTask(taskId, blueprintFile, startPos, rotation, floorLevel);
            final ServerWorld serverWorld = player.getServerWorld();

            if (serverWorld instanceof FortressServerWorld fortressWorld) {
                Runnable executeBuildTask = () -> fortressWorld.getTaskManager().addTask(task);
                if (floorLevel > 0) {
                    final SimpleSelectionTask digTask = blueprintManager.createDigTask(taskId, startPos, floorLevel, blueprintFile, rotation);
                    digTask.addFinishListener(executeBuildTask);

                    fortressWorld.getTaskManager().addTask(digTask);
                } else {
                    executeBuildTask.run();
                }

            }
        }
    }
}
