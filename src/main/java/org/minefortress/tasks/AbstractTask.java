package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.minefortress.network.ClientboundTaskExecutedPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.tasks.interfaces.Task;
import org.minefortress.utils.PathUtils;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

public abstract class AbstractTask implements Task {

    protected static final int PART_SIZE = 3;

    // CAUTION: not actually unique
    private final UUID id;
    private final TaskType taskType;
    protected BlockPos startingBlock;
    protected BlockPos endingBlock;

    protected final Queue<Pair<BlockPos, BlockPos>> parts = new ArrayDeque<>();

    protected int totalParts;
    private int completedParts;

    protected AbstractTask(UUID id, TaskType taskType, BlockPos startingBlock, BlockPos endingBlock) {
        this.id = id;
        this.taskType = taskType;
        this.startingBlock = startingBlock;
        this.endingBlock = endingBlock;
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
    public void finishPart(ServerWorld world) {
        completedParts++;
        if(parts.isEmpty() && totalParts <= completedParts) {
            ServerPlayerEntity randomPlayer = world.getRandomAlivePlayer();
            if(randomPlayer != null) {
                FortressServerNetworkHelper.send(randomPlayer, FortressChannelNames.FINISH_TASK, new ClientboundTaskExecutedPacket(this.getId()));
            }
        }
    }

    protected int getCompletedParts() {
        return completedParts;
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
