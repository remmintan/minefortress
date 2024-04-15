package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.ModLogger;
import net.remmintan.mods.minefortress.core.interfaces.entities.player.FortressServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundTaskExecutedPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerboundBlueprintTaskPacket implements FortressC2SPacket {
    private final String blueprintId;
    private final BlockPos startPos;
    private final BlockRotation rotation;
    private final int floorLevel;

    private final List<Integer> selectedPawns;

    public ServerboundBlueprintTaskPacket(String blueprintId, BlockPos startPos, BlockRotation rotation, int floorLevel, List<Integer> selectedPawns) {
        this.blueprintId = blueprintId;
        this.startPos = startPos;
        this.rotation = rotation;
        this.floorLevel = floorLevel;
        this.selectedPawns = selectedPawns;
    }

    public ServerboundBlueprintTaskPacket(PacketByteBuf buf) {
        this.blueprintId = buf.readString();
        this.startPos = buf.readBlockPos();
        this.rotation = buf.readEnumConstant(BlockRotation.class);
        this.floorLevel = buf.readInt();
        this.selectedPawns = new ArrayList<>();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            selectedPawns.add(buf.readInt());
        }

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(blueprintId);
        buf.writeBlockPos(startPos);
        buf.writeEnumConstant(rotation);
        buf.writeInt(floorLevel);
        buf.writeInt(selectedPawns.size());
        for (Integer selectedPawn : selectedPawns) {
            buf.writeInt(selectedPawn);
        }
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(player instanceof final FortressServerPlayerEntity fortressServerPlayer) {
            final var manager = getFortressManager(server, player);
            final var blueprintManager = fortressServerPlayer.get_ServerBlueprintManager();
            final var provider = getManagersProvider(server, player);

            Runnable executeBuildTask = () -> {
                final var taskId = UUID.randomUUID();
                final var task = blueprintManager.createTask(taskId, blueprintId, startPos, rotation, floorLevel);
                final var serverResourceManager = provider.getResourceManager();

                if(manager.isSurvival()) {
                    final var stacks = blueprintManager.getBlockDataManager().getBlockData(blueprintId, rotation).getStacks();
                    try {
                        serverResourceManager.reserveItems(taskId, stacks);
                    }catch (IllegalStateException e) {
                        ModLogger.LOGGER.error("Failed to reserve items for task " + taskId + ": " + e.getMessage());
                        FortressServerNetworkHelper.send(player, FortressChannelNames.FINISH_TASK, new ClientboundTaskExecutedPacket(taskId));
                        return;
                    }
                }
                final var ableToAssign = provider.getTaskManager().addTask(task, provider, manager, selectedPawns, player);
                if (!ableToAssign) {
                    serverResourceManager.returnReservedItems(taskId);
                }
            };
            if (floorLevel > 0) {
                final var digTask = blueprintManager.createDigTask(UUID.randomUUID(), startPos, floorLevel, blueprintId, rotation);
                digTask.addFinishListener(executeBuildTask);
                provider.getTaskManager().addTask(digTask, provider, manager, selectedPawns, player);
            } else {
                executeBuildTask.run();
            }
        }
    }
}
