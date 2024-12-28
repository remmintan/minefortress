package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerboundCutTreesTaskPacket implements FortressC2SPacket {

    private final UUID uuid;
    private final List<BlockPos> treeRoots;
    private final List<Integer> selectedPawns;
    private final List<BlockPos> selection;

    public ServerboundCutTreesTaskPacket(UUID uuid, List<BlockPos> treeRoots, List<BlockPos> selection, List<Integer> selectedPawns) {
        this.uuid = uuid;
        this.treeRoots = treeRoots;
        this.selection = selection;
        this.selectedPawns = selectedPawns;
    }

    public ServerboundCutTreesTaskPacket(PacketByteBuf buf) {
        this.uuid = buf.readUuid();
        this.treeRoots = buf.readCollection(ArrayList::new, PacketByteBuf::readBlockPos);

        selection = new ArrayList<>();
        final int selectionSize = buf.readInt();
        for (int i = 0; i < selectionSize; i++) {
            selection.add(buf.readBlockPos());
        }

        selectedPawns = new ArrayList<>();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            selectedPawns.add(buf.readInt());
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(uuid);
        buf.writeCollection(treeRoots, PacketByteBuf::writeBlockPos);

        buf.writeInt(selection.size());
        for (BlockPos blockPos : selection) {
            buf.writeBlockPos(blockPos);
        }

        buf.writeInt(selectedPawns.size());
        for (Integer selectedPawn : selectedPawns) {
            buf.writeInt(selectedPawn);
        }
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var provider = getManagersProvider(player);
        final var taskManager = provider.getTaskManager();
        final var tasksCreator = provider.getTasksCreator();
        final var cutTreesTask = tasksCreator.createCutTreesTask(uuid, treeRoots, selection);
        final var manager = getFortressManager(player);
        taskManager.addTask(cutTreesTask, provider, manager, selectedPawns, player);
    }
}
