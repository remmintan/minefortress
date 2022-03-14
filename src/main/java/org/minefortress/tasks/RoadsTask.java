package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.Colonist;
import org.minefortress.network.ClientboundTaskExecutedPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo;
import org.minefortress.tasks.block.info.DigTaskBlockInfo;
import org.minefortress.tasks.block.info.TaskBlockInfo;
import org.minefortress.tasks.interfaces.Task;
import org.minefortress.utils.BlockUtils;

import java.util.*;
import java.util.stream.Collectors;

public class RoadsTask implements Task {

    private final UUID id;
    private final TaskType taskType;
    private final Item item;

    private final List<BlockPos> blocks;
    private final Queue<TaskPart> taskParts = new ArrayDeque<>();
    private final int totalParts;
    private int finishedParts = 0;

    private final Runnable onComplete;

    public RoadsTask(UUID id, TaskType taskType, List<BlockPos> blocks, Item item) {
        this(id, taskType, blocks, item, () -> {});
    }

    public RoadsTask(UUID id, TaskType taskType, List<BlockPos> blocks, Item item, Runnable onComplete) {
        this.id = id;
        this.taskType = taskType;
        this.onComplete = onComplete;
        this.blocks = blocks;
        this.item = item;
        this.totalParts = prepareParts();
    }

    private int prepareParts() {
        final List<BlockPos> partBlocks = new ArrayList<>();
        int partCounter = 0;
        for (BlockPos block : blocks) {
            partBlocks.add(block);
            if (partCounter++ > 9) {
                final TaskPart taskPart = createTaskPart(partBlocks);
                taskParts.add(taskPart);
                partBlocks.clear();
                partCounter = 0;
            }
        }

        if(!partBlocks.isEmpty()) {
            final TaskPart taskPart = createTaskPart(partBlocks);
            taskParts.add(taskPart);
        }

        return taskParts.size();
    }

    @NotNull
    private TaskPart createTaskPart(List<BlockPos> partBlocks) {
        final BlockPos first = partBlocks.get(0);
        final BlockPos last = partBlocks.get(partBlocks.size() - 1);

        Pair<BlockPos, BlockPos> partStartAndEnd = Pair.of(first, last);

        final List<TaskBlockInfo> collect = partBlocks.stream().map(pos -> {
            if (Objects.isNull(item)) {
                return new DigTaskBlockInfo(pos);
            } else {
                return new BlockStateTaskBlockInfo(item, pos, BlockUtils.getBlockStateFromItem(item));
            }
        }).collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        return new TaskPart(partStartAndEnd, collect, this);
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
        return !taskParts.isEmpty();
    }

    @Override
    public TaskPart getNextPart(ServerWorld level) {
        return taskParts.poll();
    }

    @Override
    public void returnPart(Pair<BlockPos, BlockPos> partStartAndEnd) {
        final BlockPos partStart = partStartAndEnd.getFirst();
        final int i = blocks.indexOf(partStart);
        if(i != -1){
            final ArrayList<BlockPos> partBlocks = new ArrayList<>();
            for (int j = i; j < blocks.size(); j++) {
                if(j - i > 9) break;
                partBlocks.add(blocks.get(j));
            }
            final TaskPart taskPart = createTaskPart(partBlocks);
            taskParts.add(taskPart);
        }
    }

    @Override
    public void finishPart(ServerWorld level, TaskPart part, Colonist colonist) {
        finishedParts++;
        if(taskParts.isEmpty() && finishedParts == totalParts){
            level.getPlayers().stream().findAny().ifPresent(player -> {
                FortressServerNetworkHelper.send(player, FortressChannelNames.FINISH_TASK, new ClientboundTaskExecutedPacket(this.getId()));
            });
            onComplete.run();
        }
    }
}
