package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;

import java.util.ArrayList;
import java.util.List;

public class S2CAddClientTasksPacket implements FortressS2CPacket {

    public static final String CHANNEL = "minefortress:client_task_state";

    private final List<TaskInformationDto> tasks;

    public S2CAddClientTasksPacket(List<TaskInformationDto> tasks) {
        this.tasks = tasks;
    }

    public S2CAddClientTasksPacket(PacketByteBuf buf) {
        final int size = buf.readVarInt();
        this.tasks = new ArrayList<>(size);
        for(int i = 0; i < size; i++) {
            final var id = buf.readUuid();
            final var positions = new ArrayList<BlockPos>();
            final int positionsSize = buf.readInt();
            for(int j = 0; j < positionsSize; j++) {
                positions.add(buf.readBlockPos());
            }
            final var type = buf.readEnumConstant(TaskType.class);
            this.tasks.add(new TaskInformationDto(id, positions, type));
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(tasks.size());
        for(TaskInformationDto task: tasks) {
            buf.writeUuid(task.id());
            final var blockPositions = task.positions();
            buf.writeInt(blockPositions.size());
            for(BlockPos pos: blockPositions) {
                buf.writeBlockPos(pos);
            }
            buf.writeEnumConstant(task.type());
        }
    }

    @Override
    public void handle(MinecraftClient client) {
        CoreModUtils.getClientTasksHolder().orElseThrow().addTasks(tasks);
    }
}
