package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.apache.logging.log4j.core.jmx.Server;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.network.ClientboundTaskExecutedPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.tasks.block.info.TaskBlockInfo;
import org.minefortress.tasks.interfaces.Task;
import org.minefortress.utils.PathUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    private final List<Runnable> taskFinishListeners = new ArrayList<>();
    private final List<BlockPos> specialBlocks = new ArrayList<>();
    private Block specialBlock;

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
    public void finishPart(TaskPart part, Colonist colonsit) {
        completedParts++;

        ServerWorld world = (ServerWorld) colonsit.world;

        if(this.taskType == TaskType.BUILD) {
            this.checkAndPutSpecialBlocksInPart(part, colonsit);
        } else if(this.taskType == TaskType.REMOVE) {

        }

        if(parts.isEmpty() && totalParts <= completedParts) {
            ServerPlayerEntity randomPlayer = world.getRandomAlivePlayer();
            if(randomPlayer != null) {
                sendFinishTaskNotificationToPlayer(randomPlayer);
            }

            if(this.taskType == TaskType.BUILD) {
                colonsit.doActionOnMasterPlayer(player -> {
                    if(!this.specialBlocks.isEmpty()) {
                        final FortressServerManager fortressServerManager = player.getFortressServerManager();
                        if(fortressServerManager != null) {
                            fortressServerManager.addSpecialBlocks(this.specialBlock, this.specialBlocks);
                        }
                    }
                });
            }


            taskFinishListeners.forEach(Runnable::run);
        }
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

    private void checkAndPutSpecialBlocksInPart(TaskPart part, Colonist colonist) {
        final List<TaskBlockInfo> blocks = part.getBlocks();
        if(blocks == null || blocks.isEmpty()) return;
        final TaskBlockInfo firstBlockInfo = blocks.stream().findFirst().get();
        final Item placingItem = firstBlockInfo.getPlacingItem();
        if(!(placingItem instanceof final BlockItem blockItem)) return;

        colonist.doActionOnMasterPlayer(player -> {
            final FortressServerManager fortressServerManager = player.getFortressServerManager();
            final Block block = blockItem.getBlock();
            final boolean blockSpecial = fortressServerManager.isBlockSpecial(block);
            if(blockSpecial) {
                final List<BlockPos> pos = blocks.stream().map(TaskBlockInfo::getPos).collect(Collectors.toList());
                this.specialBlock = block;
                this.specialBlocks.addAll(pos);
            }
        });
    }

}
