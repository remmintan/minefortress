package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.minefortress.network.ClientboundTaskExecutedPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.tasks.block.info.DigTaskBlockInfo;
import org.minefortress.tasks.block.info.TaskBlockInfo;
import org.minefortress.tasks.interfaces.Task;
import org.minefortress.utils.TreeBlocks;
import org.minefortress.utils.TreeHelper;

import java.util.*;

public class CutTreesTask implements Task {

    private final UUID uuid;
    private final Queue<BlockPos> treeRoots;
    private final int totalRootCount;

    private int removedRoots = 0;

    public CutTreesTask(UUID uuid, List<BlockPos> treeRoots) {
        this.uuid = uuid;
        this.treeRoots = new ArrayDeque<>(treeRoots);
        this.totalRootCount = treeRoots.size();
    }

    @Override
    public UUID getId() {
        return uuid;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.REMOVE;
    }

    @Override
    public boolean hasAvailableParts() {
        return !treeRoots.isEmpty();
    }

    @Override
    public TaskPart getNextPart(ServerWorld level) {
        final BlockPos root = treeRoots.remove();
        final TaskBlockInfo rootBlockInfo = new DigTaskBlockInfo( root);
        return new TaskPart(Pair.of(root, root), Collections.singletonList(rootBlockInfo), this);
    }

    @Override
    public void returnPart(Pair<BlockPos, BlockPos> partStartAndEnd) {
        final BlockPos root = partStartAndEnd.getFirst();
        treeRoots.add(root);
    }

    @Override
    public void finishPart(ServerWorld level, TaskPart part) {
        if(part != null && part.getStartAndEnd() != null && part.getStartAndEnd().getFirst() != null) {
            final BlockPos root = part.getStartAndEnd().getFirst();
            final Optional<TreeBlocks> treeBlocks = TreeHelper.getTreeBlocks(root, level);
            if(treeBlocks.isPresent()) {
                final TreeBlocks tree = treeBlocks.get();
                tree.getTreeBlocks().forEach(blockPos -> {
                    level.removeBlock(blockPos, false);
                });
                tree.getLeavesBlocks().forEach(blockPos -> {
                    level.removeBlock(blockPos, false);
                });
            }
        }

        removedRoots++;
        if(treeRoots.isEmpty() && removedRoots <= totalRootCount) {
            level.getPlayers().stream().findAny().ifPresent(player -> {
                FortressServerNetworkHelper.send(player, FortressChannelNames.FINISH_TASK, new ClientboundTaskExecutedPacket(this.getId()));
            });
        }
    }
}
