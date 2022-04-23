package org.minefortress.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;
import org.minefortress.tasks.RoadsTask;
import org.minefortress.tasks.TaskManager;
import org.minefortress.tasks.TaskType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ServerboundRoadsTaskPacket implements FortressServerPacket {

    private final UUID digUuid;
    private final UUID placeUuid;

    private final List<BlockPos> blocks;

    public ServerboundRoadsTaskPacket(UUID digUuid, UUID placeUuid, List<BlockPos> blocks) {
        this.digUuid = digUuid;
        this.placeUuid = placeUuid;
        this.blocks = Collections.unmodifiableList(blocks);
    }

    public ServerboundRoadsTaskPacket(PacketByteBuf buf) {
        digUuid = buf.readUuid();
        placeUuid = buf.readUuid();
        blocks = buf.readCollection(ArrayList::new, PacketByteBuf::readBlockPos);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(digUuid);
        buf.writeUuid(placeUuid);
        buf.writeCollection(blocks, PacketByteBuf::writeBlockPos);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final FortressServerPlayerEntity fortressPlayer = (FortressServerPlayerEntity) player;
        final TaskManager taskManager = fortressPlayer.getTaskManager();
        final var fortressServerManager = fortressPlayer.getFortressServerManager();
        final ItemStack itemInHand = player.getStackInHand(Hand.MAIN_HAND);
        final RoadsTask buildTask = new RoadsTask(placeUuid, TaskType.BUILD, blocks, itemInHand.getItem());
        final Runnable onDigComplete = () -> {
            taskManager.addTask(buildTask, fortressServerManager);
        };

        final RoadsTask digTask = new RoadsTask(digUuid, TaskType.REMOVE, blocks, null, onDigComplete);
        taskManager.addTask(digTask, fortressServerManager);
    }
}
