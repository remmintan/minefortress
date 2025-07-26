package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;

import java.util.stream.LongStream;

public class S2CAddClientTaskPacket implements FortressS2CPacket {

    public static final String CHANNEL = "minefortress_client_task_state";

    private final TaskInformationDto task;

    public S2CAddClientTaskPacket(TaskInformationDto task) {
        this.task = task;
    }

    public S2CAddClientTaskPacket(PacketByteBuf buf) {
        final var pos = buf.readBlockPos();
        final var positions = LongStream.of(buf.readLongArray()).mapToObj(BlockPos::fromLong).toList();
        final var type = buf.readEnumConstant(TaskType.class);
        this.task = new TaskInformationDto(pos, positions, type);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(task.pos());
        final var positionsArray = task.positions().stream().mapToLong(BlockPos::asLong).toArray();
        buf.writeLongArray(positionsArray);
        buf.writeEnumConstant(task.type());
    }

    @Override
    public void handle(MinecraftClient client) {
        ClientModUtils.getClientTasksHolder().orElseThrow().addTask(task);
    }
}
