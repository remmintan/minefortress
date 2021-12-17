package org.minefortress.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.minefortress.interfaces.FortressServerWorld;
import org.minefortress.network.interfaces.FortressServerPacket;
import org.minefortress.selections.SelectionType;
import org.minefortress.tasks.Task;
import org.minefortress.tasks.TaskManager;
import org.minefortress.tasks.TaskType;

import java.util.Optional;
import java.util.UUID;

public class ServerboundColonistTaskPacket implements FortressServerPacket {

    private final UUID id;
    private final TaskType taskType;
    private final BlockPos start;
    private final BlockPos end;
    private final HitResult hitResult;
    private final SelectionType selectionType;

    public ServerboundColonistTaskPacket(UUID id, TaskType taskType, BlockPos start, BlockPos end, HitResult hitResult, SelectionType selectionType) {
        this.id = id;
        this.taskType = taskType;
        this.start = start;
        this.end = end;
        this.hitResult = hitResult;
        this.selectionType = selectionType;
    }

    public ServerboundColonistTaskPacket(PacketByteBuf buffer) {
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
        this.selectionType = buffer.readEnumConstant(SelectionType.class);
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
        buffer.writeEnumConstant(selectionType);
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

    public SelectionType getSelectionType() {
        return selectionType;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        UUID id = this.getId();
        TaskType taskType = this.getTaskType();
        TaskManager taskManager = ((FortressServerWorld)player.getServerWorld()).getTaskManager();
        BlockPos startingBlock = this.getStart();
        BlockPos endingBlock = this.getEnd();
        HitResult hitResult = this.getHitResult();
        SelectionType selectionType = this.getSelectionType();
        Task task = new Task(id, taskType, startingBlock, endingBlock, hitResult, selectionType);
        if(task.getTaskType() == TaskType.BUILD) {
            final ItemStack itemInHand = player.getStackInHand(Hand.MAIN_HAND);
            if(itemInHand != ItemStack.EMPTY) {
                task.setPlacingItem(itemInHand.getItem());
            } else {
//                throw new IllegalStateException();
            }
        }

        taskManager.addTask(task);
    }
}
