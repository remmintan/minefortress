package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ServerboundRoadsTaskPacket implements FortressC2SPacket {

    private final UUID digUuid;
    private final UUID placeUuid;

    private final List<BlockPos> blocks;
    private final List<Integer> selectedPawns;

    public ServerboundRoadsTaskPacket(UUID digUuid, UUID placeUuid, List<BlockPos> blocks, List<Integer> selectedPawns) {
        this.digUuid = digUuid;
        this.placeUuid = placeUuid;
        this.blocks = Collections.unmodifiableList(blocks);
        this.selectedPawns =  selectedPawns;
    }

    public ServerboundRoadsTaskPacket(PacketByteBuf buf) {
        digUuid = buf.readUuid();
        placeUuid = buf.readUuid();
        blocks = buf.readCollection(ArrayList::new, PacketByteBuf::readBlockPos);
        selectedPawns = new ArrayList<>();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            selectedPawns.add(buf.readInt());
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(digUuid);
        buf.writeUuid(placeUuid);
        buf.writeCollection(blocks, PacketByteBuf::writeBlockPos);
        buf.writeInt(selectedPawns.size());
        for (Integer selectedPawn : selectedPawns) {
            buf.writeInt(selectedPawn);
        }
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var provider = getManagersProvider(server, player);
        final var taskManager = provider.getTaskManager();
        final var resourceManager = provider.getResourceManager();

        final var stackInHand = player.getStackInHand(Hand.MAIN_HAND);
        final var item = stackInHand.getItem();
        final var buildTask = taskManager.createRoadsTask(digUuid, TaskType.BUILD, placeUuid, blocks, item);
        final var manager = getFortressManager(server, player);
        final Runnable onDigComplete = () -> taskManager.addTask(buildTask, provider, manager, selectedPawns, player);

        if(manager.isSurvival())
            resourceManager.reserveItems(placeUuid, Collections.singletonList(resourceManager.createItemInfo(item, blocks.size())));

        final var digTask = taskManager.createRoadsTask(digUuid, TaskType.REMOVE, placeUuid, blocks, item, onDigComplete);
        taskManager.addTask(digTask, provider, manager, selectedPawns, player);
    }
}
