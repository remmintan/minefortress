package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart;
import net.remmintan.mods.minefortress.core.utils.PathUtils;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundTaskExecutedPacket;

import java.util.*;

public abstract class AbstractTask implements ITask {

    protected static final int PART_SIZE = 3;

    // CAUTION: not actually unique
    protected final UUID id;
    protected final TaskType taskType;
    protected BlockPos startingBlock;
    protected BlockPos endingBlock;

    protected final Queue<Pair<BlockPos, BlockPos>> parts = new ArrayDeque<>();

    protected int totalParts;
    private int completedParts;

    private final List<Runnable> taskFinishListeners = new ArrayList<>();

    protected AbstractTask(UUID id, TaskType taskType, BlockPos startingBlock, BlockPos endingBlock) {
        this.id = id;
        this.taskType = taskType;
        this.startingBlock = startingBlock;
        this.endingBlock = endingBlock;
    }

    public BlockPos getStartingBlock() {
        return startingBlock;
    }

    public BlockPos getEndingBlock() {
        return endingBlock;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public TaskType getTaskType() {
        return taskType;
    }


    @Override
    public boolean hasAvailableParts() {
        return !this.parts.isEmpty();
    }

    @Override
    public void returnPart(Pair<BlockPos, BlockPos> part) {
        parts.add(part);
    }

    @Override
    public void prepareTask() {
        BlockPos.Mutable cursor = this.startingBlock.mutableCopy();
        final Vec3i direction = PathUtils.getDirection(startingBlock, endingBlock);
        do {
            BlockPos start = cursor.toImmutable();
            BlockPos end = createPartEnd(start, direction);
            parts.add(Pair.of(start, end));

            if(end.getX() * direction.getX() >= endingBlock.getX() * direction.getX()) {
                if (end.getZ() * direction.getZ() >= endingBlock.getZ() * direction.getZ()) {
                    break;
                } else {
                    cursor.setX(startingBlock.getX());
                    cursor.move(0, 0, PART_SIZE * direction.getZ());
                }
            } else {
                cursor.move(direction.getX() * PART_SIZE, 0, 0);
            }
        } while (true);

        this.totalParts = parts.size();
    }

    @Override
    public void finishPart(ITaskPart part, IWorkerPawn colonsit) {
        completedParts++;
        if(parts.isEmpty() && totalParts <= completedParts) {
            colonsit.getMasterPlayer().ifPresent(this::sendFinishTaskNotificationToPlayer);
            taskFinishListeners.forEach(Runnable::run);
        }
    }

    @Override
    public List<TaskInformationDto> toTaskInformationDto() {
        final var blocks = new ArrayList<BlockPos>();
        BlockPos.iterate(startingBlock, endingBlock).forEach(it -> blocks.add(it.toImmutable()));
        return Collections.singletonList(new TaskInformationDto(id, blocks, taskType));
    }

    protected void sendFinishTaskNotificationToPlayer(ServerPlayerEntity randomPlayer) {
        FortressServerNetworkHelper.send(randomPlayer, FortressChannelNames.FINISH_TASK, new ClientboundTaskExecutedPacket(this.getId()));
    }

    public void addFinishListener(Runnable listener) {
        taskFinishListeners.add(listener);
    }

    protected int getCompletedParts() {
        return completedParts;
    }

    protected static Item getItemFromState(BlockState state) {
        final Block block = state.getBlock();
        return block.asItem();
    }

    private BlockPos createPartEnd(BlockPos start, Vec3i direction) {
        BlockPos.Mutable cursor = start.mutableCopy();
        cursor.setY(endingBlock.getY());
        cursor.move((PART_SIZE-1) * direction.getX(), 0, (PART_SIZE-1)*direction.getZ());
        if(cursor.getX() * direction.getX() > endingBlock.getX() * direction.getX()) {
            cursor.setX(endingBlock.getX());
        }
        if(cursor.getZ() * direction.getZ() > endingBlock.getZ() * direction.getZ()) {
            cursor.setZ(endingBlock.getZ());
        }
        return cursor.toImmutable();
    }

}
