package org.minefortress.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;
import org.minefortress.tasks.CutTreesTask;
import org.minefortress.tasks.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerboundCutTreesTaskPacket implements FortressServerPacket {

    private final UUID uuid;
    private final List<BlockPos> treeRoots;

    public ServerboundCutTreesTaskPacket(UUID uuid, List<BlockPos> treeRoots) {
        this.uuid = uuid;
        this.treeRoots = treeRoots;
    }

    public ServerboundCutTreesTaskPacket(PacketByteBuf buf) {
        this.uuid = buf.readUuid();
        this.treeRoots = buf.readCollection(ArrayList::new, PacketByteBuf::readBlockPos);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(uuid);
        buf.writeCollection(treeRoots, PacketByteBuf::writeBlockPos);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var fortressServerManager = this.getFortressServerManager(server, player);
        TaskManager taskManager = fortressServerManager.getTaskManager();
        final CutTreesTask cutTreesTask = new CutTreesTask(uuid, treeRoots);
        taskManager.addTask(cutTreesTask, fortressServerManager);
    }
}
