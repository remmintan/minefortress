package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerboundCutTreesTaskPacket implements FortressC2SPacket {

    private final UUID uuid;
    private final List<BlockPos> treeRoots;
    private final List<Integer> selectedPawns;

    public ServerboundCutTreesTaskPacket(UUID uuid, List<BlockPos> treeRoots, List<Integer> selectedPawns) {
        this.uuid = uuid;
        this.treeRoots = treeRoots;
        this.selectedPawns = selectedPawns;
    }

    public ServerboundCutTreesTaskPacket(PacketByteBuf buf) {
        this.uuid = buf.readUuid();
        this.treeRoots = buf.readCollection(ArrayList::new, PacketByteBuf::readBlockPos);
        this.selectedPawns = buf.readCollection(ArrayList::new, PacketByteBuf::readInt);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(uuid);
        buf.writeCollection(treeRoots, PacketByteBuf::writeBlockPos);
        buf.writeCollection(selectedPawns, PacketByteBuf::writeInt);
    }

    @Override
    public void handle(@NotNull MinecraftServer server, @NotNull ServerPlayerEntity player) {
        final var provider = getManagersProvider(player);
        final var taskManager = provider.getTaskManager();
        final var tasksCreator = provider.getTasksCreator();
        final var cutTreesTask = tasksCreator.createCutTreesTask(treeRoots);
        taskManager.addTask(cutTreesTask, selectedPawns, player);
    }
}
