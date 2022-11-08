package org.minefortress.network.c2s;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;
import org.minefortress.selections.SelectionType;
import org.minefortress.selections.ServerSelectionType;
import org.minefortress.tasks.SimpleSelectionTask;
import org.minefortress.tasks.TaskManager;
import org.minefortress.tasks.TaskType;

import java.util.Optional;
import java.util.UUID;

public class ServerboundSimpleSelectionTaskPacket implements FortressServerPacket {

    private final UUID id;
    private final TaskType taskType;
    private final BlockPos start;
    private final BlockPos end;
    private final HitResult hitResult;
    private final String selectionType;

    public ServerboundSimpleSelectionTaskPacket(UUID id, TaskType taskType, BlockPos start, BlockPos end, HitResult hitResult, SelectionType selectionType) {
        this.id = id;
        this.taskType = taskType;
        this.start = start;
        this.end = end;
        this.hitResult = hitResult;
        this.selectionType = selectionType.name();
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
        final var fortressServerManager = this.getFortressServerManager(server, player);
        UUID id = this.getId();
        TaskType taskType = this.getTaskType();
        TaskManager taskManager = fortressServerManager.getTaskManager();
        BlockPos startingBlock = this.getStart();
        BlockPos endingBlock = this.getEnd();
        HitResult hitResult = this.getHitResult();
        ServerSelectionType selectionType = this.getSelectionType();
        SimpleSelectionTask simpleSelectionTask = new SimpleSelectionTask(id, taskType, startingBlock, endingBlock, hitResult, selectionType);
        if(simpleSelectionTask.getTaskType() == TaskType.BUILD) {
            final ItemStack itemInHand = player.getStackInHand(Hand.MAIN_HAND);
            if(itemInHand != ItemStack.EMPTY) {
                simpleSelectionTask.setPlacingItem(itemInHand.getItem());
            } else {
                throw new IllegalStateException();
            }
        }

        taskManager.addTask(simpleSelectionTask, fortressServerManager);
    }
}
