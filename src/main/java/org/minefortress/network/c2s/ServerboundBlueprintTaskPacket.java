package org.minefortress.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.minefortress.blueprints.manager.ServerBlueprintManager;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.fortress.resources.server.ServerResourceManager;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.network.interfaces.FortressC2SPacket;
import org.minefortress.network.s2c.ClientboundTaskExecutedPacket;
import org.minefortress.tasks.BlueprintTask;
import org.minefortress.tasks.SimpleSelectionTask;
import java.util.List;
import java.util.UUID;

public class ServerboundBlueprintTaskPacket implements FortressC2SPacket {

    private final UUID taskId;
    private final String blueprintId;
    private final BlockPos startPos;
    private final BlockRotation rotation;
    private final int floorLevel;

    public ServerboundBlueprintTaskPacket(UUID taskId, String blueprintId, BlockPos startPos, BlockRotation rotation, int floorLevel) {
        this.taskId = taskId;
        this.blueprintId = blueprintId;
        this.startPos = startPos;
        this.rotation = rotation;
        this.floorLevel = floorLevel;
    }

    public ServerboundBlueprintTaskPacket(PacketByteBuf buf) {
        this.taskId = buf.readUuid();
        this.blueprintId = buf.readString();
        this.startPos = buf.readBlockPos();
        this.rotation = buf.readEnumConstant(BlockRotation.class);
        this.floorLevel = buf.readInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(taskId);
        buf.writeString(blueprintId);
        buf.writeBlockPos(startPos);
        buf.writeEnumConstant(rotation);
        buf.writeInt(floorLevel);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(player instanceof final FortressServerPlayerEntity fortressServerPlayer) {
            final var fortressServerManager = this.getFortressServerManager(server, player);
            final ServerBlueprintManager blueprintManager = fortressServerPlayer.getServerBlueprintManager();
            final BlueprintTask task = blueprintManager.createTask(taskId, blueprintId, startPos, rotation, floorLevel);

            if(fortressServerManager.isSurvival()) {
                final var serverResourceManager = fortressServerManager.getServerResourceManager();
                final var stacks = blueprintManager.getBlockDataManager().getBlockData(blueprintId, rotation).getStacks();
                try {
                    serverResourceManager.reserveItems(taskId, stacks);
                }catch (IllegalStateException e) {
                    LogManager.getLogger().error("Failed to reserve items for task " + taskId + ": " + e.getMessage());
                    FortressServerNetworkHelper.send(player, FortressChannelNames.FINISH_TASK, new ClientboundTaskExecutedPacket(taskId));
                    return;
                }
            }
            Runnable executeBuildTask = () -> fortressServerManager.getTaskManager().addTask(task, fortressServerManager);
            if (floorLevel > 0) {
                final SimpleSelectionTask digTask = blueprintManager.createDigTask(taskId, startPos, floorLevel, blueprintId, rotation);
                digTask.addFinishListener(executeBuildTask);

                fortressServerManager.getTaskManager().addTask(digTask, fortressServerManager);
            } else {
                executeBuildTask.run();
            }
        }
    }
}
