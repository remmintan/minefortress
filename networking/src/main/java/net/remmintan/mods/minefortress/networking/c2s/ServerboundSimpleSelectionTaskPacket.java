package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import net.remmintan.mods.minefortress.core.interfaces.selections.ServerSelectionType;

import java.util.Optional;
import java.util.UUID;

public class ServerboundSimpleSelectionTaskPacket implements FortressC2SPacket {

    private final UUID id;
    private final TaskType taskType;
    private final BlockPos start;
    private final BlockPos end;
    private final HitResult hitResult;
    private final String selectionType;

    public ServerboundSimpleSelectionTaskPacket(UUID id, TaskType taskType, BlockPos start, BlockPos end, HitResult hitResult, String selectionType) {
        this.id = id;
        this.taskType = taskType;
        this.start = start;
        this.end = end;
        this.hitResult = hitResult;
        this.selectionType = selectionType;
    }

    public ServerboundSimpleSelectionTaskPacket(PacketByteBuf buffer) {
        this.id = buffer.readUuid();
        this.taskType = buffer.readEnumConstant(TaskType.class);
        this.start = buffer.readBlockPos();
        this.end = buffer.readBlockPos();
        HitResult.Type hitType = buffer.readEnumConstant(HitResult.Type.class);
        if(hitType == HitResult.Type.BLOCK) {
            this.hitResult = buffer.readBlockHitResult();
        } else {
            this.hitResult = null;
        }
        this.selectionType = buffer.readString();
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeUuid(id);
        buffer.writeEnumConstant(this.taskType);
        buffer.writeBlockPos(this.start);
        buffer.writeBlockPos(this.end);
        HitResult.Type type = Optional.ofNullable(this.hitResult).map(HitResult::getType).orElse(HitResult.Type.MISS);
        buffer.writeEnumConstant(type);
        if(type == HitResult.Type.BLOCK) {
            buffer.writeBlockHitResult((BlockHitResult) this.hitResult);
        }
        buffer.writeString(selectionType);
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public BlockPos getStart() {
        return start;
    }

    public BlockPos getEnd() {
        return end;
    }

    public HitResult getHitResult() {
        return hitResult;
    }

    public ServerSelectionType getSelectionType() {
        return ServerSelectionType.valueOf(selectionType);
    }

    public UUID getId() {
        return id;
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var provider = getManagersProvider(server, player);
        final var id = this.getId();
        final var taskType = this.getTaskType();
        final var taskManager = provider.getTaskManager();
        final var startingBlock = this.getStart();
        final var endingBlock = this.getEnd();
        final var hitResult = this.getHitResult();
        final var selectionType = this.getSelectionType();
        final var task = taskManager.createSelectionTask(id, taskType, startingBlock, endingBlock, selectionType, hitResult, player);
        final var manager = getFortressManager(server, player);

        taskManager.addTask(task, provider, manager);
    }
}
