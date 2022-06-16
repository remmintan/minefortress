package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.controls.DigControl;
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
    public TaskPart getNextPart(ServerWorld level, Colonist colonist) {
        if(!treeRoots.isEmpty()) {
            final BlockPos root = treeRoots.remove();
            final TaskBlockInfo rootBlockInfo = new DigTaskBlockInfo( root);
            return new TaskPart(Pair.of(root, root), Collections.singletonList(rootBlockInfo), this);
        } else {
            return null;
        }
    }

    @Override
    public void returnPart(Pair<BlockPos, BlockPos> partStartAndEnd) {
        final BlockPos root = partStartAndEnd.getFirst();
        treeRoots.add(root);
    }

    @Override
    public void finishPart(TaskPart part, Colonist colonist) {
        final ServerWorld world = (ServerWorld) colonist.world;
        if(part != null && part.getStartAndEnd() != null && part.getStartAndEnd().getFirst() != null) {
            final BlockPos root = part.getStartAndEnd().getFirst();
            final Optional<TreeBlocks> treeOpt = TreeHelper.getTreeBlocks(root.up(), world);
            if(treeOpt.isPresent()) {
                final TreeBlocks tree = treeOpt.get();
                tree.getTreeBlocks().forEach(blockPos -> {
                    DigControl.addDropToTheResourceManager(world, blockPos, colonist);
                    world.breakBlock(blockPos, false, colonist);
                    world.emitGameEvent(colonist, GameEvent.BLOCK_DESTROY, blockPos);

                });
                tree.getLeavesBlocks().forEach(blockPos -> {
                    DigControl.addDropToTheResourceManager(world, blockPos, colonist);
                    world.breakBlock(blockPos, false, colonist);
                    world.emitGameEvent(colonist, GameEvent.BLOCK_DESTROY, blockPos);
                });
            }
        }

        removedRoots++;
        if(treeRoots.isEmpty() && removedRoots <= totalRootCount) {
            world.getPlayers().stream().findAny().ifPresent(player -> {
                FortressServerNetworkHelper.send(player, FortressChannelNames.FINISH_TASK, new ClientboundTaskExecutedPacket(this.getId()));
            });
        }
    }
}
