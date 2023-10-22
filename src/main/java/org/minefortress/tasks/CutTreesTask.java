package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart;
import org.minefortress.entity.Colonist;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;
import org.minefortress.entity.ai.controls.DigControl;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundTaskExecutedPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import org.minefortress.tasks.block.info.DigTaskBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;
import net.remmintan.gobi.helpers.TreeBlocks;
import net.remmintan.gobi.helpers.TreeHelper;

import java.util.*;

public class CutTreesTask implements ITask {

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
    public ITaskPart getNextPart(ServerWorld level, IWorkerPawn colonist) {
        if(!treeRoots.isEmpty()) {
            final BlockPos root = treeRoots.remove();
            final ITaskBlockInfo rootBlockInfo = new DigTaskBlockInfo( root);
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
    public void finishPart(ITaskPart part, IWorkerPawn colonist) {
        final ServerWorld world = colonist.getServerWorld();
        if(part != null && part.getStartAndEnd() != null && part.getStartAndEnd().getFirst() != null) {
            final BlockPos root = part.getStartAndEnd().getFirst();
            final Optional<TreeBlocks> treeOpt = TreeHelper.getTreeBlocks(root.up(), world);
            if(treeOpt.isPresent()) {
                final TreeBlocks tree = treeOpt.get();
                tree.getTreeBlocks().forEach(blockPos -> {
                    DigControl.addDropToTheResourceManager(world, blockPos, (Colonist) colonist);
                    world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
                    world.emitGameEvent((Entity) colonist, GameEvent.BLOCK_DESTROY, blockPos);

                });
                tree.getLeavesBlocks().forEach(blockPos -> {
                    DigControl.addDropToTheResourceManager(world, blockPos, (Colonist) colonist);
                    world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
                    world.emitGameEvent((Entity) colonist, GameEvent.BLOCK_DESTROY, blockPos);
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
