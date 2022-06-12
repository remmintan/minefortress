package org.minefortress.network;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.fortress.resources.server.ServerResourceManager;
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
        final var fortressServerManager = this.getFortressServerManager(server, player);
        final var taskManager = fortressServerManager.getTaskManager();
        final var serverResourceManager = fortressServerManager.getServerResourceManager();
        final var itemInHand = player.getStackInHand(Hand.MAIN_HAND);
        final var item = itemInHand.getItem();
        final var buildTask = new RoadsTask(placeUuid, TaskType.BUILD, blocks, item);
        final Runnable onDigComplete = () -> taskManager.addTask(buildTask, fortressServerManager);

        if(fortressServerManager.isSurvival())
            serverResourceManager.reserveItems(placeUuid, Collections.singletonList(new ItemInfo(item, blocks.size())));

        final var digTask = new RoadsTask(digUuid, TaskType.REMOVE, blocks, null, onDigComplete);
        taskManager.addTask(digTask, fortressServerManager);
    }
}
