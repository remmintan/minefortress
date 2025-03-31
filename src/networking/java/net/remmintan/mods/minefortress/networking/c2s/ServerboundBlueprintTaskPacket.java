package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.ModLogger;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import net.remmintan.mods.minefortress.core.utils.ServerExtensionsKt;
import net.remmintan.mods.minefortress.core.utils.ServerPlayerEntityExtensionsKt;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundTaskExecutedPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerboundBlueprintTaskPacket implements FortressC2SPacket {
    private final String blueprintId;
    private final BlockPos startPos;
    private final BlockRotation rotation;
    private final int floorLevel;
    private final List<Integer> selectedPawns;
    private final BlockPos upgradedBuildingPos;

    public ServerboundBlueprintTaskPacket(String blueprintId, BlockPos startPos, BlockRotation rotation, int floorLevel, List<Integer> selectedPawns, BlockPos upgradedBuildingPos) {
        this.blueprintId = blueprintId;
        this.startPos = startPos;
        this.rotation = rotation;
        this.floorLevel = floorLevel;
        this.selectedPawns = selectedPawns;
        this.upgradedBuildingPos = upgradedBuildingPos;
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
        this.upgradedBuildingPos = buf.readNullable(PacketByteBuf::readBlockPos);
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
        buf.writeNullable(upgradedBuildingPos, PacketByteBuf::writeBlockPos);
    }

    @Override
    public void handle(@NotNull MinecraftServer server, @NotNull ServerPlayerEntity player) {
        final var provider = getManagersProvider(player);
        final var blueprintManager = ServerPlayerEntityExtensionsKt.getManagersProvider(player).get_BlueprintManager();

        if (upgradedBuildingPos != null) {
            provider.getBuildingsManager().destroyBuilding(upgradedBuildingPos);
        }

        Runnable executeBuildTask = () -> {
            final var taskId = UUID.randomUUID();
            final var task = blueprintManager.createTask(taskId, blueprintId, startPos, rotation);
            final var serverResourceManager = provider.getResourceManager();

            if (ServerExtensionsKt.isSurvivalFortress(server)) {
                final var stacks = blueprintManager.getBlockDataManager().getBlockData(blueprintId, rotation).getStacks();
                try {
                    serverResourceManager.reserveItems(taskId, stacks);
                } catch (IllegalStateException e) {
                    ModLogger.LOGGER.error("Failed to reserve items for task " + taskId + ": " + e.getMessage());
                    FortressServerNetworkHelper.send(player, FortressChannelNames.FINISH_TASK, new ClientboundTaskExecutedPacket(taskId));
                    return;
                }
            }
            provider.getTaskManager().addTask(task, selectedPawns, player);
        };
        if (floorLevel > 0) {
            final var digTask = blueprintManager.createDigTask(UUID.randomUUID(), startPos, floorLevel, blueprintId, rotation);
            digTask.addFinishListener(executeBuildTask);
            provider.getTaskManager().addTask(digTask, selectedPawns, player);
        } else {
            executeBuildTask.run();
        }
    }
}
